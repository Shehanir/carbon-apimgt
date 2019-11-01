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
import org.wso2.carbon.apimgt.impl.crd.*;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CRDJet {

    private static final Logger log = LoggerFactory.getLogger(CRDJet.class);

    private static String API_CRD_GROUP = "wso2.com";
    private static String API_CRD_NAME = "apis." +  API_CRD_GROUP;


    public CustomResourceDefinition setUpCrds(KubernetesClient client) {

        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        log.info("Found "+ crdsItems.size() + " CRD(s)");
        CustomResourceDefinition apiCRD = null;

        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();

            if (metadata != null) {
                String name = metadata.getName();
                log.info("    " + name + " => " + metadata.getSelfLink());

                if (API_CRD_NAME.equals(name)) {
                    apiCRD = crd;
                }
            }
        }

        if (apiCRD != null) {
            log.info("Found CRD: " + apiCRD.getMetadata().getSelfLink());
        } else {
            apiCRD = new CustomResourceDefinitionBuilder().withApiVersion("apiextensions.k8s.io/v1beta1").
                    withNewMetadata().withName(this.API_CRD_NAME).endMetadata().withNewSpec().withGroup(this.API_CRD_GROUP).
                    withVersion("v1beta1").withScope("Namespaced").withNewNames().withKind("API").withShortNames("api").
                    withPlural("apis").endNames().endSpec().build();

            client.customResourceDefinitions().create(apiCRD);
            log.info("Created CRD " + apiCRD.getMetadata().getName());
        }

        KubernetesDeserializer.registerCustomKind(this.API_CRD_GROUP+"/v1beta1", "API", APICrd.class);
        return apiCRD;
    }

    public void createAPICRD(String configMapName, String apiName, String namespace, int replicas, KubernetesClient client, CustomResourceDefinition apiCRD) {

        Definition definition = new Definition();
        definition.setType("swagger");
        definition.setConfigMapName(configMapName);

        APICrdSpec apiCrdSpec = new APICrdSpec();
        apiCrdSpec.setDefinition(definition);
        apiCrdSpec.setMode("privateJet");
        apiCrdSpec.setReplicas(replicas);

        APICrd apiCrd = new APICrd();
        apiCrd.setSpec(apiCrdSpec);
        apiCrd.setApiVersion(this.API_CRD_GROUP + "/v1beta1");
        apiCrd.setKind("API");
        ObjectMeta meta = new ObjectMeta();
        meta.setName(apiName);
        apiCrd.setMetadata(meta);

        NonNamespaceOperation<APICrd, APICrdList, DoneableAPICrd, Resource<APICrd, DoneableAPICrd>> apiCRDClient =
                client.customResources(apiCRD, APICrd.class, APICrdList.class, DoneableAPICrd.class);

        apiCRDClient = ((MixedOperation<APICrd, APICrdList, DoneableAPICrd, Resource<APICrd, DoneableAPICrd>>)
                apiCRDClient).inNamespace(namespace);

        APICrd created = apiCRDClient.createOrReplace(apiCrd);
        log.info("Upserted " + apiCrd);
    }
}
