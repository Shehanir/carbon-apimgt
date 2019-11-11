/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.containermgt;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.*;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.security.*;

import java.util.List;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

/**
 * This is responsible for publishing the api in private jet mode.
 */
public class PrivateJet {

    private static final Logger log = LoggerFactory.getLogger(PrivateJet.class);

    /**
     * This method creates the k8s client, deploy the swagger definition as a config
     * map and deploy the custom resource definitions.
     *
     * @param swaggerDefinition , swagger definition of the api as a string
     * @param apiIdentifier     , APIIdentifier object for the api
     * @param swaggerCreator
     */
    public void publishInPrivateJetMode(String swaggerDefinition, APIIdentifier apiIdentifier, String tenant_conf,
                                        SwaggerCreator swaggerCreator) throws ParseException {

        TenantConfReader newReader = new TenantConfReader();
        K8sClient k8sClient = newReader.readTenant(tenant_conf);

        boolean publishInPrivateJet = ((!(k8sClient.getSaToken().equals("")))
                && (!(k8sClient.getSaToken().equals(""))));

        if (publishInPrivateJet) {

            KubernetesClient client = k8sClient.createClient();
            if (swaggerCreator.isSecurityOauth2()) {

                applyOauthSecret(client);
                applySecretCert(client);
                applyOauthSecurity(client, apiIdentifier);
            }
            if (swaggerCreator.isSecurityJWT()) {

                applySecretCert(client);
                applyJWTSecurity(client, apiIdentifier);
            }

            String configmapName = apiIdentifier.getApiName().toLowerCase() + ".v" + apiIdentifier.getVersion();

            io.fabric8.kubernetes.client.dsl.Resource<ConfigMap, DoneableConfigMap> configMapResource
                    = client.configMaps().inNamespace(k8sClient.getNamespace()).withName(configmapName);

            ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().withNewMetadata().
                    withName(configmapName).withNamespace(k8sClient.getNamespace()).endMetadata().
                    withApiVersion("v1").addToData(apiIdentifier.getApiName() + ".json", swaggerDefinition).build());

            log.info("Created ConfigMap at " + configMap.getMetadata().getSelfLink() + " data" + configMap.getData());
            applyAPICustomResourceDefinition(client, configmapName, k8sClient, apiIdentifier);
            log.info("Successfully Published in Private-Jet Mode");

        } else {

            log.error("Can not Publish In Private Jet Mode");
            if (k8sClient.getMasterURL().equals("")) {
                log.info("Master URL for the Kubernetes Cluster Has Not been Provided");
            }

            if (k8sClient.getSaToken().equals("")) {
                log.info("Service Account Token for the Kubernetes Cluster Has Not been Provided");
            }
        }
    }

    private void applyJWTSecurity(KubernetesClient client, APIIdentifier apiIdentifier) {

        CustomResourceDefinitionList customResourceDefinitionList = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> customResourceDefinitionItems = customResourceDefinitionList.getItems();
        CustomResourceDefinition jwtCustomResourceDefinition = null;

        for (CustomResourceDefinition crd : customResourceDefinitionItems) {
            ObjectMeta metadata = crd.getMetadata();

            if (metadata != null) {
                String name = metadata.getName();
                log.info("    " + name + " => " + metadata.getSelfLink());

                if (SECURITY_CRD_NAME.equals(name)) {
                    jwtCustomResourceDefinition = crd;
                }
            }
        }

        if (jwtCustomResourceDefinition != null) {
            log.info("Found Security CRD: " + jwtCustomResourceDefinition.getMetadata().getSelfLink());
        } else {
            jwtCustomResourceDefinition = new CustomResourceDefinitionBuilder().withApiVersion(K8_CRD_VERSION).
                    withNewMetadata().withName(SECURITY_CRD_NAME).endMetadata().withNewSpec().withGroup(API_CRD_GROUP).
                    withVersion(API_CRD_VERSION).withScope(API_CRD_SCOPE).withNewNames().withKind(SECURITY_KIND).
                    withShortNames(SECURITY_KIND_SHORT).withPlural(SECURITY_KIND_PLURAL).
                    withListKind(SECURITY_KIND_LIST).endNames().endSpec().build();

            client.customResourceDefinitions().create(jwtCustomResourceDefinition);
            log.info("Created CRD " + jwtCustomResourceDefinition.getMetadata().getName());
        }

        KubernetesDeserializer.registerCustomKind(API_VERSION, SECURITY_KIND,
                JWTSecurityCustomResourceDefinition.class);

        NonNamespaceOperation<JWTSecurityCustomResourceDefinition, JWTSecurityCustomResourceDefinitionList,
                DoneableJWTSecurityCustomResourceDefinition, Resource<JWTSecurityCustomResourceDefinition,
                DoneableJWTSecurityCustomResourceDefinition>> jwtSecurityCrdClient =
                client.customResources(jwtCustomResourceDefinition,
                        JWTSecurityCustomResourceDefinition.class,
                        JWTSecurityCustomResourceDefinitionList.class,
                        DoneableJWTSecurityCustomResourceDefinition.class);

        jwtSecurityCrdClient = ((MixedOperation<JWTSecurityCustomResourceDefinition,
                JWTSecurityCustomResourceDefinitionList,
                DoneableJWTSecurityCustomResourceDefinition,
                Resource<JWTSecurityCustomResourceDefinition, DoneableJWTSecurityCustomResourceDefinition>>)
                jwtSecurityCrdClient).inNamespace(client.getNamespace());

        JWTSecurityCustomResourceDefinitionSpec spec = new JWTSecurityCustomResourceDefinitionSpec();
        JWTSecurityCustomResourceDefinition jwtSecurityCustomResourceDefinition =
                new JWTSecurityCustomResourceDefinition();

        ObjectMeta jwtSecurityMeta = new ObjectMeta();
        jwtSecurityMeta.setName(apiIdentifier.getApiName().toLowerCase() + OPENAPI_SECURITY_SCHEMA_KEY_JWT);
        jwtSecurityMeta.setNamespace(client.getNamespace());

        jwtSecurityCustomResourceDefinition.setMetadata(jwtSecurityMeta);
        jwtSecurityCrdClient.createOrReplace(jwtSecurityCustomResourceDefinition);
        log.info(SECURITY_KIND_SHORT + "." + API_CRD_GROUP + "/" +
                jwtSecurityCustomResourceDefinition.getMetadata().getName() + " created");
    }

    private void applyOauthSecurity(KubernetesClient client, APIIdentifier apiIdentifier) {

        CustomResourceDefinitionList customResourceDefinitionList = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> customResourceDefinitionItems = customResourceDefinitionList.getItems();
        CustomResourceDefinition oauthCustomResourceDefinition = null;

        for (CustomResourceDefinition crd : customResourceDefinitionItems) {
            ObjectMeta metadata = crd.getMetadata();

            if (metadata != null) {
                String name = metadata.getName();
                log.info("    " + name + " => " + metadata.getSelfLink());

                if (SECURITY_CRD_NAME.equals(name)) {
                    oauthCustomResourceDefinition = crd;
                }
            }
        }

        if (oauthCustomResourceDefinition != null) {
            log.info("Found Security CRD: " + oauthCustomResourceDefinition.getMetadata().getSelfLink());
        } else {
            oauthCustomResourceDefinition = new CustomResourceDefinitionBuilder().withApiVersion(K8_CRD_VERSION).
                    withNewMetadata().withName(SECURITY_CRD_NAME).endMetadata().withNewSpec().withGroup(API_CRD_GROUP).
                    withVersion(API_CRD_VERSION).withScope(API_CRD_SCOPE).withNewNames().withKind(SECURITY_KIND).
                    withShortNames(SECURITY_KIND_SHORT).withPlural(SECURITY_KIND_PLURAL).withListKind(SECURITY_KIND_LIST)
                    .endNames().endSpec().build();

            client.customResourceDefinitions().create(oauthCustomResourceDefinition);
            log.info("Created CRD " + oauthCustomResourceDefinition.getMetadata().getName());
        }

        KubernetesDeserializer.registerCustomKind(API_VERSION, SECURITY_KIND,
                OauthCustomResourceDefinition.class);

        NonNamespaceOperation<OauthCustomResourceDefinition, OauthCustomResourceDefinitionList,
                DoneableOauthCustomResourceDefinition, Resource<OauthCustomResourceDefinition,
                DoneableOauthCustomResourceDefinition>> oauthSecurityCrdClient =
                client.customResources(oauthCustomResourceDefinition, OauthCustomResourceDefinition.class,
                        OauthCustomResourceDefinitionList.class, DoneableOauthCustomResourceDefinition.class);

        oauthSecurityCrdClient = ((MixedOperation<OauthCustomResourceDefinition,
                OauthCustomResourceDefinitionList,
                DoneableOauthCustomResourceDefinition,
                Resource<OauthCustomResourceDefinition, DoneableOauthCustomResourceDefinition>>)
                oauthSecurityCrdClient).inNamespace(client.getNamespace());

        OauthCustomResourceDefinitionSpec spec = new OauthCustomResourceDefinitionSpec();
        OauthCustomResourceDefinition oauthCustomResourceDef = new OauthCustomResourceDefinition();
        oauthCustomResourceDef.setSpec(spec);

        ObjectMeta oauthSecurityMeta = new ObjectMeta();
        oauthSecurityMeta.setName(apiIdentifier.getApiName().toLowerCase() + OPENAPI_SECURITY_SCHEMA_KEY_OAUTH2);
        oauthSecurityMeta.setNamespace(client.getNamespace());

        oauthCustomResourceDef.setMetadata(oauthSecurityMeta);
        oauthSecurityCrdClient.createOrReplace(oauthCustomResourceDef);
        log.info(SECURITY_KIND_SHORT + "." + API_CRD_GROUP + "/" +
                oauthCustomResourceDef.getMetadata().getName() + " created");

    }

    private void applySecretCert(KubernetesClient client) {

        CustomResourceDefinitionList customResourceDefinitionList = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> customResourceDefinitionItems = customResourceDefinitionList.getItems();
        CustomResourceDefinition certSecretCustomResourceDefinition = null;

        for (CustomResourceDefinition crd : customResourceDefinitionItems) {
            ObjectMeta metadata = crd.getMetadata();

            if (metadata != null) {
                String name = metadata.getName();
                log.info("    " + name + " => " + metadata.getSelfLink());

                if (SECRET_CRD_NAME.equals(name)) {
                    certSecretCustomResourceDefinition = crd;
                }
            }
        }

        if (certSecretCustomResourceDefinition != null) {
            log.info("Found Secret CRD: " + certSecretCustomResourceDefinition.getMetadata().getSelfLink());
        } else {
            certSecretCustomResourceDefinition = new CustomResourceDefinitionBuilder().withApiVersion(K8_CRD_VERSION).
                    withNewMetadata().withName(SECRET_CRD_NAME).endMetadata().withNewSpec().withGroup(API_CRD_GROUP).
                    withVersion(V1).withScope(API_CRD_SCOPE).withNewNames().withKind(SECRETS_KIND).
                    withShortNames(SECRETS_KIND_SHORT).withPlural(SECRETS_KIND_PLURAL).endNames().endSpec().build();

            client.customResourceDefinitions().create(certSecretCustomResourceDefinition);
            log.info("Created CRD " + certSecretCustomResourceDefinition.getMetadata().getName());
        }

        KubernetesDeserializer.registerCustomKind(API_CRD_GROUP + "/" + API_CRD_VERSION, SECRETS_KIND,
                SecretCustomResourceDefinition.class);

        NonNamespaceOperation<SecretCustomResourceDefinition, SecretCustomResourceDefinitionList,
                DoneableSecretCustomResourceDefinition, Resource<SecretCustomResourceDefinition,
                DoneableSecretCustomResourceDefinition>> secretCrdClient =
                client.customResources(certSecretCustomResourceDefinition, SecretCustomResourceDefinition.class,
                        SecretCustomResourceDefinitionList.class, DoneableSecretCustomResourceDefinition.class);

        secretCrdClient = ((MixedOperation<SecretCustomResourceDefinition,
                SecretCustomResourceDefinitionList,
                DoneableSecretCustomResourceDefinition,
                Resource<SecretCustomResourceDefinition, DoneableSecretCustomResourceDefinition>>)
                secretCrdClient).inNamespace(client.getNamespace());

        SecretCustomResourceDefinitionData data = new SecretCustomResourceDefinitionData();
        SecretCustomResourceDefinition secretCustomResourceDefinition = new SecretCustomResourceDefinition();
        secretCustomResourceDefinition.setData(data);
        secretCustomResourceDefinition.setKind(SECRETS_KIND);

        ObjectMeta certSecretMeta = new ObjectMeta();
        certSecretMeta.setName(SECURITY_CERTIFICATE);
        certSecretMeta.setNamespace(client.getNamespace());

        secretCustomResourceDefinition.setMetadata(certSecretMeta);
        secretCrdClient.createOrReplace(secretCustomResourceDefinition);
        log.info("secret/" + secretCustomResourceDefinition.getMetadata().getName() + " created");
    }

    private void applyOauthSecret(KubernetesClient client) {

        CustomResourceDefinitionList customResourceDefinitionList = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> customResourceDefinitionItems = customResourceDefinitionList.getItems();
        CustomResourceDefinition oauthSecretCustomResourceDefinition = null;

        for (CustomResourceDefinition crd : customResourceDefinitionItems) {
            ObjectMeta metadata = crd.getMetadata();

            if (metadata != null) {
                String name = metadata.getName();
                log.info("    " + name + " => " + metadata.getSelfLink());

                if (SECRET_CRD_NAME.equals(name)) {
                    oauthSecretCustomResourceDefinition = crd;
                }
            }
        }

        if (oauthSecretCustomResourceDefinition != null) {
            log.info("Found Secret CRD: " + oauthSecretCustomResourceDefinition.getMetadata().getSelfLink());
        } else {
            oauthSecretCustomResourceDefinition = new CustomResourceDefinitionBuilder().withApiVersion(K8_CRD_VERSION).
                    withNewMetadata().withName(SECRET_CRD_NAME).endMetadata().withNewSpec().withGroup(API_CRD_GROUP).
                    withVersion(V1).withScope(API_CRD_SCOPE).withNewNames().withKind(SECRETS_KIND).
                    withShortNames(SECRETS_KIND_SHORT).withPlural(SECRETS_KIND_PLURAL).endNames().endSpec().build();

            client.customResourceDefinitions().create(oauthSecretCustomResourceDefinition);
            log.info("Created CRD " + oauthSecretCustomResourceDefinition.getMetadata().getName());
        }

        KubernetesDeserializer.registerCustomKind(API_CRD_GROUP + "/" + API_CRD_VERSION, SECRETS_KIND,
                OauthSecretCustomResourceDefinition.class);

        NonNamespaceOperation<OauthSecretCustomResourceDefinition, OauthSecretCustomResourceDefinitionList,
                DoneableOauthSecretCustomResourceDefinition, Resource<OauthSecretCustomResourceDefinition,
                DoneableOauthSecretCustomResourceDefinition>> oauthSecretCrdClient =
                client.customResources(oauthSecretCustomResourceDefinition,
                        OauthSecretCustomResourceDefinition.class,
                        OauthSecretCustomResourceDefinitionList.class,
                        DoneableOauthSecretCustomResourceDefinition.class);

        oauthSecretCrdClient = ((MixedOperation<OauthSecretCustomResourceDefinition,
                OauthSecretCustomResourceDefinitionList,
                DoneableOauthSecretCustomResourceDefinition,
                Resource<OauthSecretCustomResourceDefinition, DoneableOauthSecretCustomResourceDefinition>>)
                oauthSecretCrdClient).inNamespace(client.getNamespace());

        OauthSecretCustomResourceDefinitionData data = new OauthSecretCustomResourceDefinitionData();
        OauthSecretCustomResourceDefinition oauthSecretCustomResourceDef = new OauthSecretCustomResourceDefinition();
        oauthSecretCustomResourceDef.setData(data);
        oauthSecretCustomResourceDef.setKind(SECRETS_KIND);
        ObjectMeta metaOauthSecret = new ObjectMeta();
        metaOauthSecret.setName(OAUTH2_CREDENTIALS_NAME);
        metaOauthSecret.setNamespace(client.getNamespace());
        oauthSecretCustomResourceDef.setMetadata(metaOauthSecret);

        oauthSecretCrdClient.createOrReplace(oauthSecretCustomResourceDef);
        log.info("secret/" + oauthSecretCustomResourceDef.getMetadata().getName() + " created");
    }

    private void applyAPICustomResourceDefinition(KubernetesClient client, String configmapName,
                                                  K8sClient k8sClient, APIIdentifier apiIdentifier) {

        CustomResourceDefinitionList customResourceDefinitionList = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> customResourceDefinitionItems = customResourceDefinitionList.getItems();
        log.info("Found " + customResourceDefinitionItems.size() + " CRD(s)");
        CustomResourceDefinition apiCustomResourceDefinition = null;

        for (CustomResourceDefinition crd : customResourceDefinitionItems) {
            ObjectMeta metadata = crd.getMetadata();

            if (metadata != null) {
                String name = metadata.getName();
                log.info("    " + name + " => " + metadata.getSelfLink());

                if (API_CRD_NAME.equals(name)) {
                    apiCustomResourceDefinition = crd;
                }
            }
        }

        if (apiCustomResourceDefinition != null) {
            log.info("Found CRD: " + apiCustomResourceDefinition.getMetadata().getSelfLink());
        } else {
            apiCustomResourceDefinition = new CustomResourceDefinitionBuilder().withApiVersion(K8_CRD_VERSION).
                    withNewMetadata().withName(API_CRD_NAME).endMetadata().withNewSpec().withGroup(API_CRD_GROUP).
                    withVersion(API_CRD_VERSION).withScope(API_CRD_SCOPE).withNewNames().withKind(CRD_KIND).
                    withShortNames(CRD_KIND_SHORT).withPlural(CRD_KIND_PLURAL).endNames().endSpec().build();

            client.customResourceDefinitions().create(apiCustomResourceDefinition);
            log.info("Created CRD " + apiCustomResourceDefinition.getMetadata().getName());
        }

        KubernetesDeserializer.registerCustomKind(API_CRD_GROUP + "/" + API_CRD_VERSION, CRD_KIND,
                APICustomResourceDefinition.class);
        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                DoneableAPICustomResourceDefinition>> apiCrdClient = client.customResources(apiCustomResourceDefinition,
                APICustomResourceDefinition.class,
                APICustomResourceDefinitionList.class,
                DoneableAPICustomResourceDefinition.class);

        apiCrdClient = ((MixedOperation<APICustomResourceDefinition,
                APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition,
                Resource<APICustomResourceDefinition, DoneableAPICustomResourceDefinition>>) apiCrdClient).
                inNamespace(k8sClient.getNamespace());

        Definition definition = new Definition();
        definition.setType(SWAGGER);
        definition.setconfigmapName(configmapName);

        APICustomResourceDefinitionSpec apiCustomResourceDefinitionSpec = new APICustomResourceDefinitionSpec();
        apiCustomResourceDefinitionSpec.setDefinition(definition);
        apiCustomResourceDefinitionSpec.setMode(MODE);
        apiCustomResourceDefinitionSpec.setReplicas(k8sClient.getReplicas());

        APICustomResourceDefinition apiCustomResourceDef = new APICustomResourceDefinition();
        apiCustomResourceDef.setSpec(apiCustomResourceDefinitionSpec);
        apiCustomResourceDef.setApiVersion(API_CRD_GROUP + "/" + API_CRD_VERSION);
        apiCustomResourceDef.setKind(CRD_KIND);
        ObjectMeta meta = new ObjectMeta();
        meta.setName(apiIdentifier.getApiName().toLowerCase());
        meta.setNamespace(k8sClient.getNamespace());
        apiCustomResourceDef.setMetadata(meta);

        apiCrdClient.createOrReplace(apiCustomResourceDef);
        log.info(API_CRD_NAME + "/" + apiCustomResourceDef.getMetadata().getName() + "created");
    }
}
