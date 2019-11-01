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
package org.wso2.carbon.apimgt.impl;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import org.wso2.carbon.apimgt.impl.crd.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.wso2.carbon.apimgt.impl.APIConstants.*;

public class CustomResourceDefinitionCreator {

    private static final Logger log = LoggerFactory.getLogger(CustomResourceDefinitionCreator.class);

    public CustomResourceDefinition setUpCustomResourceDefinitions(KubernetesClient client) {

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

        KubernetesDeserializer.registerCustomKind(API_CRD_GROUP + "/" + API_CRD_VERSION, CRD_KIND, APICustomResourceDefinition.class);
        return apiCustomResourceDefinition;
    }

    public void createAPICustomResourceDefinition(String configMapName, String apiName, String namespace, int replicas,
                                                  KubernetesClient client, CustomResourceDefinition apiCRD) {

        Definition definition = new Definition();
        definition.setType(CONFIG_MAP_TYPE);
        definition.setConfigMapName(configMapName);

        APICustomResourceDefinitionSpec apiCustomResourceDefinitionSpec = new APICustomResourceDefinitionSpec();
        apiCustomResourceDefinitionSpec.setDefinition(definition);
        apiCustomResourceDefinitionSpec.setMode(MODE);
        apiCustomResourceDefinitionSpec.setReplicas(replicas);

        APICustomResourceDefinition apiCustomResourceDefinition = new APICustomResourceDefinition();
        apiCustomResourceDefinition.setSpec(apiCustomResourceDefinitionSpec);
        apiCustomResourceDefinition.setApiVersion(API_CRD_GROUP + "/" + API_CRD_VERSION);
        apiCustomResourceDefinition.setKind(CRD_KIND);
        ObjectMeta meta = new ObjectMeta();
        meta.setName(apiName);
        apiCustomResourceDefinition.setMetadata(meta);

        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList, DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition, DoneableAPICustomResourceDefinition>> apiCRDClient =
                client.customResources(apiCRD, APICustomResourceDefinition.class, APICustomResourceDefinitionList.class, DoneableAPICustomResourceDefinition.class);

        apiCRDClient = ((MixedOperation<APICustomResourceDefinition, APICustomResourceDefinitionList, DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition, DoneableAPICustomResourceDefinition>>)
                apiCRDClient).inNamespace(namespace);

        APICustomResourceDefinition created = apiCRDClient.createOrReplace(apiCustomResourceDefinition);
        log.info("Upserted " + apiCustomResourceDefinition);
    }
}
