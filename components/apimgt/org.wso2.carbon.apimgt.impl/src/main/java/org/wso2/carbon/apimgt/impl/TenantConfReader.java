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

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

public class TenantConfReader {

    public K8sClient readTenant(String tenantConf) throws IOException, ParseException {

        JSONParser jsonParser = new JSONParser();
        Object tenantObject = jsonParser.parse(tenantConf);
        JSONObject tenant_conf = (JSONObject) tenantObject;

        K8sClient k8sClient = new K8sClient();

        k8sClient.setMasterURL((String) tenant_conf.get("k8sMasterURL"));
        k8sClient.setSaToken((String) tenant_conf.get("saToken"));
        k8sClient.setNamespace((String) tenant_conf.get("namespace"));
        k8sClient.setReplicas(Math.toIntExact((long) tenant_conf.get("replicas")));

        return k8sClient;
    }
}
