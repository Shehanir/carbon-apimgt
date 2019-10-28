package org.wso2.carbon.apimgt.impl;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class Client {

    private String masterURL;
    private String saToken;
    private String namespace;
    private int replicas;

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public String getMasterURL() {
        return masterURL;
    }

    public void setSaToken(String saToken) {
        this.saToken = saToken;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public int getReplicas() {
        return replicas;
    }

    public String getSaToken() {
        return saToken;
    }

    public String getNamespace() {
        return namespace;
    }


    public KubernetesClient createClient() {
        Config config = new ConfigBuilder().withMasterUrl(this.masterURL).withOauthToken(this.saToken).withNamespace(this.namespace)
                .build();

        KubernetesClient client = new DefaultKubernetesClient(config);
        return client;
    }
}
