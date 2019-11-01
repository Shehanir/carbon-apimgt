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
import org.wso2.carbon.apimgt.impl.CRDJet;
import org.wso2.carbon.apimgt.impl.Client;
import org.wso2.carbon.apimgt.impl.CMap;


public class PrivateJet {

    private static final Logger log = LoggerFactory.getLogger(PrivateJet.class);

    public void publishPrivateJet(String masterURL, String saToken, String namespace, String swaggerDef, int replicas, APIIdentifier apiIdentifier, Client k8sClient) {

        KubernetesClient client = k8sClient.createClient();

        String configMapName = apiIdentifier.getApiName().toLowerCase() + ".v" + apiIdentifier.getVersion();
        CMap cMap = new CMap();
        cMap.setConfMapName(configMapName);

        CRDJet crdJet = new CRDJet();
        CustomResourceDefinition customResourceDefinition = crdJet.setUpCrds(client);
        crdJet.createAPICRD(configMapName, apiIdentifier.getApiName().toLowerCase(), namespace, replicas, client, customResourceDefinition);

        cMap.publishCMap(swaggerDef, namespace, client, apiIdentifier);

    }
}
