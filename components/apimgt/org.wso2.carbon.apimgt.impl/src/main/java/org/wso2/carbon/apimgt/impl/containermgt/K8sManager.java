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
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.*;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

public class K8sManager implements ContainerManager {

    private static final Logger log = LoggerFactory.getLogger(K8sManager.class);

    private String masterURL;
    private String saToken;
    private String namespace;
    private int replicas;
    private String clusterName;
    private String jwtSecurityCRName = "";
    private String oauthSecurityCRName = "";
    private String basicAuthSecurityCRName ="";
    private OpenShiftClient openShiftClient;

    @Override
    public void initManager(Map<String, String> parameters) {

        setValues(parameters);
        setClient();
    }

    @Override
    public void DeployAPI(API api, APIIdentifier apiIdentifier)
            throws RegistryException, ParseException, APIManagementException {

        Registry registry = getRegistryService().getGovernanceUserRegistry();

        SwaggerCreator swaggerCreator = new SwaggerCreator(
                basicAuthSecurityCRName, jwtSecurityCRName, oauthSecurityCRName);
        String swagger = swaggerCreator.
                getOASDefinitionForPublisher(api, OASParserUtil.getAPIDefinition(apiIdentifier, registry));


        if (!saToken.equals("") && !masterURL.equals("")) {

            /**
             * configmapName would be "apiname.v" + "apiVersion"
             */
            String configmapName = apiIdentifier.getApiName().toLowerCase() + ".v" + apiIdentifier.getVersion();

            io.fabric8.kubernetes.client.dsl.Resource<ConfigMap, DoneableConfigMap> configMapResource
                    = openShiftClient.configMaps().inNamespace(namespace).withName(configmapName);

            ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().withNewMetadata().
                    withName(configmapName).withNamespace(namespace).endMetadata().
                    withApiVersion("v1").addToData(apiIdentifier.getApiName() + ".json", swagger).build());

            /**
             * Remove this later!
             * Outputs the swagger of API
             */
            log.info("Created ConfigMap at " + configMap.getMetadata().getSelfLink() + " data" + configMap.getData());
            applyAPICustomResourceDefinition(openShiftClient, configmapName, replicas, apiIdentifier);
            log.info("Successfully Published in Private-Jet Mode");

            if (swaggerCreator.isSecurityOauth2() && oauthSecurityCRName.equals("")) {
                log.warn("OAuth2 security custom resource name has not been provided");
                log.info("The API will not be able to invoke via OAuth2 tokens");
            }

            if (swaggerCreator.isSecurityOauth2() && jwtSecurityCRName.equals("")) {
                log.warn("JWT security custom resource name has not been provided");
                log.info("The API will not be able to invoke via jwt tokens");
            }

            if (swaggerCreator.isSecurityBasicAuth() && basicAuthSecurityCRName.equals("")) {
                log.warn("Basic-Auth security custom resource name has not been provided");
                log.info("The API will not be able to invoke via basic-auth tokens");
            }
        } else {
            log.warn("Master URL and/or Service-account Token hasn't been Provided.");
            log.warn("The API will not be Published in " + clusterName);
        }
    }


    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    private void applyAPICustomResourceDefinition(KubernetesClient client, String configmapName,
                                                  int replicas, APIIdentifier apiIdentifier) {

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
                inNamespace(client.getNamespace());

        Definition definition = new Definition();
        definition.setType(SWAGGER);
        definition.setconfigmapName(configmapName);

        APICustomResourceDefinitionSpec apiCustomResourceDefinitionSpec = new APICustomResourceDefinitionSpec();
        apiCustomResourceDefinitionSpec.setDefinition(definition);
        apiCustomResourceDefinitionSpec.setMode(MODE);
        apiCustomResourceDefinitionSpec.setReplicas(replicas);

        APICustomResourceDefinition apiCustomResourceDef = new APICustomResourceDefinition();
        apiCustomResourceDef.setSpec(apiCustomResourceDefinitionSpec);
        apiCustomResourceDef.setApiVersion(API_VERSION);
        apiCustomResourceDef.setKind(CRD_KIND);
        ObjectMeta meta = new ObjectMeta();
        meta.setName(apiIdentifier.getApiName().toLowerCase());
        meta.setNamespace(client.getNamespace());
        apiCustomResourceDef.setMetadata(meta);

        apiCrdClient.createOrReplace(apiCustomResourceDef);
        log.info(API_CRD_NAME + "/" + apiCustomResourceDef.getMetadata().getName() + "created");
    }

    private void setValues(Map<String, String> clusterParameters) {

        this.masterURL = clusterParameters.get(MASTER_URL);
        this.saToken = clusterParameters.get(SATOKEN);
        this.namespace = clusterParameters.get(NAMESPACE);
        this.replicas = Integer.parseInt(clusterParameters.get(REPLICAS));
        this.clusterName = clusterParameters.get(CLUSTER_NAME);
        this.jwtSecurityCRName = clusterParameters.get(JWT_SECURITY_CR_NAME);
        this.oauthSecurityCRName = clusterParameters.get(OAUTH2_SECURITY_CR_NAME);
        this.basicAuthSecurityCRName = clusterParameters.get(BASICAUTH_SECURITY_CR_NAME);
    }

    private void setClient() {

        Config config = new ConfigBuilder().withMasterUrl(masterURL)
                .withOauthToken(saToken).withNamespace(namespace).build();

        this.openShiftClient = new DefaultOpenShiftClient(config);

    }
}
