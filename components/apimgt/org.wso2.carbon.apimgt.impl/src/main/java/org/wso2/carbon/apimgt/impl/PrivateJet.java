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

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;


public class PrivateJet {

    private static final Logger log = LoggerFactory.getLogger(PrivateJet.class);

    public void publishInPrivateJetMode(String namespace, String swaggerDefinition, int replicas,
                                        APIIdentifier apiIdentifier, K8sClient k8sClient) {

        KubernetesClient client = k8sClient.createClient();

        String configMapName = apiIdentifier.getApiName().toLowerCase() + ".v" + apiIdentifier.getVersion();
        ConfigMapDeployment configMapDeployment = new ConfigMapDeployment();
        configMapDeployment.setConfigMapName(configMapName);

        CustomResourceDefinitionCreator customResourceDefinitionCreator = new CustomResourceDefinitionCreator();
        CustomResourceDefinition customResourceDefinition =
                customResourceDefinitionCreator.setUpCustomResourceDefinitions(client);

        customResourceDefinitionCreator.createAPICustomResourceDefinition(
                configMapName, apiIdentifier.getApiName().toLowerCase(), namespace, replicas, client,
                customResourceDefinition);

        configMapDeployment.deployConfigMap(swaggerDefinition, namespace, client, apiIdentifier);
        log.info("Successfully Published in Private-Jet Mode");

    }
}
