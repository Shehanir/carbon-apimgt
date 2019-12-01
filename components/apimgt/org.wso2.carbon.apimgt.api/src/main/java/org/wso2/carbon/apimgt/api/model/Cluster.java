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

package org.wso2.carbon.apimgt.api.model;

/**
 * This class represent the Cluster
 */
public class Cluster {

    private String clusterName;
    private String masterURL;
    private String saToken;
    private String namespace;
    private int replicas;
    private String jwtSecurityCRName;
    private String oauth2SecurityCRName;
    private String basicAuthSecurityCRName;
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getBasicAuthSecurityCRName() {
        return basicAuthSecurityCRName;
    }

    public void setBasicAuthSecurityCRName(String basicAuthSecurityCRName) {
        this.basicAuthSecurityCRName = basicAuthSecurityCRName;
    }

    public String getJwtSecurityCRName() {
        return jwtSecurityCRName;
    }

    public void setJwtSecurityCRName(String jwtSecurityCRName) {
        this.jwtSecurityCRName = jwtSecurityCRName;
    }

    public String getOauth2SecurityCRName() {
        return oauth2SecurityCRName;
    }

    public void setOauth2SecurityCRName(String oauth2SecurityCRName) {
        this.oauth2SecurityCRName = oauth2SecurityCRName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMasterURL() {
        return masterURL;
    }

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public String getSaToken() {
        return saToken;
    }

    public void setSaToken(String saToken) {
        this.saToken = saToken;
    }
}
