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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;

public class CMap {

    private static final Logger log = LoggerFactory.getLogger(CMap.class);
    private String confMapName;

    public void setConfMapName(String confMapName) {
        this.confMapName = confMapName;
    }

    public void publishCMap(String swagger, String namespace, KubernetesClient client, APIIdentifier apiIdentifier) {

        io.fabric8.kubernetes.client.dsl.Resource<ConfigMap, DoneableConfigMap> configMapResource = client.configMaps().
                inNamespace(namespace).withName(this.confMapName);

        ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().withNewMetadata().
                withName(this.confMapName).withNamespace(namespace).endMetadata().
                withApiVersion("v1").addToData(apiIdentifier.getApiName()+".json", swagger).build());

        log.info("Upserted ConfigMap at " + configMap.getMetadata().getSelfLink() + " data" + configMap.getData());
    }
}
