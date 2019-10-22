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

    public void publishPrivateJet(String masterURL, String saToken, String namespace, String swaggerDef, int replicas, APIIdentifier apiIdentifier) {

        Client k8sclient = new Client();
        k8sclient.setMasterURL(masterURL);
        k8sclient.setNamespace(namespace);
        k8sclient.setSaToken(saToken);
        KubernetesClient client = k8sclient.createClient();

        String configMapName = apiIdentifier.getApiName().toLowerCase() + ".v" + apiIdentifier.getVersion();
        CMap cMap = new CMap();
        cMap.setConfMapName(configMapName);

        CRDJet crdJet = new CRDJet();
        CustomResourceDefinition customResourceDefinition = crdJet.setUpCrds(client);
        crdJet.createAPICRD(configMapName, apiIdentifier.getApiName(), namespace, replicas, client, customResourceDefinition);

        cMap.publishCMap(swaggerDef, namespace, client, apiIdentifier);

    }
}
