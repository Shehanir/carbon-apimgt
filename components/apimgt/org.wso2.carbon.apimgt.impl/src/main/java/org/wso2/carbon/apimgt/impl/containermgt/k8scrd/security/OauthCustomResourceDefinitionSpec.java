/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.containermgt.k8scrd.security;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)

public class OauthCustomResourceDefinitionSpec implements KubernetesResource {

    private String type; //OAUTH_TYPE
    private String certificate; //SECURITY_CERTIFICATE
    private String endpoint; //OAUTH2_END_POINT
    private String credentials; //OAUTH2_CREDENTIALS_NAME

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Creates the following json object
     * type: ${type}
     * certificate: ${certificate}
     * endpoint: ${endpoint}
     * credentials: ${credentials}
     *
     * @return
     */
    @Override
    public String toString() {
        return "OauthSpec{" +
                "type='" + type + '\'' +
                ", certificate='" + certificate + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", credentials='" + credentials + '\'' +
                '}';
    }
}
