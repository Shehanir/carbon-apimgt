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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is for reading the tenant-conf.json from the registry
 * and for getting the master-url and service-account token and optionally the
 * namespace and number of replicas of the cluster.
 */
public class TenantConfReader {

    /**
     * Reads the tenant-conf.json file for getting master url, service account token,
     * namespace and number of replicas.
     *
     * @param tenantConf , Content of the tenant-conf
     * @return , K8sClient whose master url, namespace, satoken and replicas are set to that in the tenant-conf.json
     * @throws ParseException , to catch the syntax errors.
     */
    public K8sClient readTenant(String tenantConf) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        Object tenantObject = jsonParser.parse(tenantConf);
        JSONObject tenant_conf = (JSONObject) tenantObject;

        K8sClient k8sClient = new K8sClient();

        k8sClient.setMasterURL((String) ((JSONObject)tenant_conf.get("K8sClusterInfo")).get("k8sMasterURL"));
        k8sClient.setSaToken((String) ((JSONObject) tenant_conf.get("K8sClusterInfo")).get("saToken"));
        k8sClient.setNamespace((String) ((JSONObject) tenant_conf.get("K8sClusterInfo")).get("namespace"));
        k8sClient.setReplicas(Math.toIntExact(((long) ((JSONObject) tenant_conf.get("K8sClusterInfo")).get("replicas"))));
        k8sClient.setBasicSecurityCustomResourceName((String) ((JSONObject)tenant_conf.get("K8sClusterInfo"))
                .get("basicSecurityCustomResourceName"));

        k8sClient.setOauthSecurityCustomResourceName((String) ((JSONObject)tenant_conf.get("K8sClusterInfo"))
                .get("oauthSecurityCustomResourceName"));

        k8sClient.setJwtSecurityCustomResourceName((String) ((JSONObject)tenant_conf.get("K8sClusterInfo"))
                .get("jwtSecurityCustomResourceName"));

        return k8sClient;
    }
}
