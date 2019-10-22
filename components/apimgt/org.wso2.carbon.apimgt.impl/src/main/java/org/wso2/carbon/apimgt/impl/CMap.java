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
