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

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * This class creates the client for accessing the kubernetes cluster.
 */
public class K8sClient {

    private String name;
    private String masterURL;
    private String saToken;
    private String namespace;
    private int replicas;
    private String basicSecurityCustomResourceName;
    private String oauthSecurityCustomResourceName;
    private String jwtSecurityCustomResourceName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterURL() {
        return masterURL;
    }

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getSaToken() {
        return saToken;
    }

    public void setSaToken(String saToken) {
        this.saToken = saToken;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getBasicSecurityCustomResourceName() {
        return basicSecurityCustomResourceName;
    }

    public void setBasicSecurityCustomResourceName(String basicSecurityCustomResourceName) {
        this.basicSecurityCustomResourceName = basicSecurityCustomResourceName;
    }

    public String getOauthSecurityCustomResourceName() {
        return oauthSecurityCustomResourceName;
    }

    public void setOauthSecurityCustomResourceName(String oauthSecurityCustomResourceName) {
        this.oauthSecurityCustomResourceName = oauthSecurityCustomResourceName;
    }

    public String getJwtSecurityCustomResourceName() {
        return jwtSecurityCustomResourceName;
    }

    public void setJwtSecurityCustomResourceName(String jwtSecurityCustomResourceName) {
        this.jwtSecurityCustomResourceName = jwtSecurityCustomResourceName;
    }

    /**
     * Creates the kubernetes client.
     *
     * @return , Kubernetes client
     */
    public KubernetesClient createClient() {
        Config config = new ConfigBuilder().withMasterUrl(this.masterURL)
                .withOauthToken(this.saToken).withNamespace(this.namespace).build();

        KubernetesClient client = new DefaultKubernetesClient(config);
        return client;
    }
}
