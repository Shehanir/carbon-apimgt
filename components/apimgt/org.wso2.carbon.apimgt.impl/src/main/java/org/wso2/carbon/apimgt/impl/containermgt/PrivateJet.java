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
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.v3.core.util.Json;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.*;

import java.util.List;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

/**
 * This is responsible for publishing the api in private jet mode.
 */
public class PrivateJet {

    private static final Logger log = LoggerFactory.getLogger(PrivateJet.class);

    /**
     * publishes an API in private jet mode
     *
     * @param apiIdentifier     , API Identifier
     * @param k8sClient
     * @param swaggerCreator    , SwaggerCreator object for getting security info
     * @param swaggerDefinition , swagger of the API
     * @throws ParseException , applies for readTenant method
     */
    public void publishInPrivateJetMode(APIIdentifier apiIdentifier, K8sClient k8sClient, SwaggerCreator swaggerCreator,
                                        String swaggerDefinition) throws ParseException {

        /**
         * If the MasterURL and SAToken is not provided
         * private jet mode will not be enabled
         */
        boolean publishInPrivateJet = ((!(k8sClient.getSaToken().equals("")))
                && (!(k8sClient.getSaToken().equals(""))));

        if (publishInPrivateJet) {

            OpenShiftClient client = k8sClient.createClient(); //creating the client

            /**
             * configmapName would be "apiname.v" + "apiVersion"
             */
            String configmapName = apiIdentifier.getApiName().toLowerCase() + ".v" + apiIdentifier.getVersion();

            io.fabric8.kubernetes.client.dsl.Resource<ConfigMap, DoneableConfigMap> configMapResource
                    = client.configMaps().inNamespace(k8sClient.getNamespace()).withName(configmapName);

            ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().withNewMetadata().
                    withName(configmapName).withNamespace(k8sClient.getNamespace()).endMetadata().
                    withApiVersion("v1").addToData(apiIdentifier.getApiName() + ".json", swaggerDefinition).build());

            /**
             * Remove this later!
             * Outputs the swagger of API
             */
            log.info("Created ConfigMap at " + configMap.getMetadata().getSelfLink() + " data" + configMap.getData());
            applyAPICustomResourceDefinition(client, configmapName, k8sClient.getReplicas(), apiIdentifier);
            log.info("Successfully Published in Private-Jet Mode");

            if (swaggerCreator.isSecurityOauth2() && k8sClient.getOauthSecurityCustomResourceName().equals("")) {
                log.warn("OAuth2 security custom resource name has not been provided");
                log.info("The API will not be able to invoke via OAuth2 tokens");
            }

            if (swaggerCreator.isSecurityOauth2() && k8sClient.getJwtSecurityCustomResourceName().equals("")) {
                log.warn("JWT security custom resource name has not been provided");
                log.info("The API will not be able to invoke via jwt tokens");
            }

            if (swaggerCreator.isSecurityBasicAuth() && k8sClient.getBasicSecurityCustomResourceName().equals("")) {
                log.warn("Basic-Auth security custom resource name has not been provided");
                log.info("The API will not be able to invoke via basic-auth tokens");
            }

            PodWatcher podWatcher = new PodWatcher();
            podWatcher.setPodList(client.pods().list());
            JSONObject pod = podWatcher.getPodStatus();
            log.info(Json.pretty(pod));

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

    /**
     * Deploys the custom resource created by APICustomResourceDefinition.class
     *
     * @param client        , Kubernetes client
     * @param configmapName , name of the configmap
     * @param replicas      , number of replicas
     * @param apiIdentifier , API Identifier
     */
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
}
