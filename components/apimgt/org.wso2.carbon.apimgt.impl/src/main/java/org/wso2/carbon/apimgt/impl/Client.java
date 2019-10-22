package org.wso2.carbon.apimgt.impl;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class Client {

    private String masterURL;
    private String saToken;
    private String namespace;

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public void setSaToken(String saToken) {
        this.saToken = saToken;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public KubernetesClient createClient() {
        Config config = new ConfigBuilder().withMasterUrl(this.masterURL).withOauthToken(this.saToken).withNamespace(this.namespace)
                .build();

        KubernetesClient client = new DefaultKubernetesClient(config);
        return client;
    }
}
