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

package org.wso2.carbon.apimgt.impl.containermgt.model;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMRegistryService;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ClusterInfoImpl implements ClusterInfo {

    @Override
    public List<Cluster> getAllClusters(String tenantDomain) throws UserStoreException, RegistryException, ParseException {

        List<Cluster> clusterList = new ArrayList<>();
        String content = getTenantConfigContent(tenantDomain);
        APIUtil util = new APIUtil();
        JSONObject allClients = util.getClusterInfoFromConfig(content);
        Set<String> names = allClients.keySet();
        Iterator<String> iterator = names.iterator();

        while (iterator.hasNext()) {

            Cluster cluster = new Cluster();
            String name = iterator.next();
            JSONObject properties = (JSONObject) allClients.get(name);
            cluster.setClusterName(name);
            cluster.setMasterURL(properties.get("MasterURL").toString());
            cluster.setNamespace(properties.get("Namespace").toString());
            cluster.setSaToken(properties.get("SAToken").toString());

            Config config = new ConfigBuilder().withMasterUrl(cluster.getMasterURL())
                    .withOauthToken(cluster.getSaToken()).withNamespace(cluster.getNamespace()).build();

            OpenShiftClient client = new DefaultOpenShiftClient(config);
            cluster.setPodList(client.pods().list().getItems());
            clusterList.add(cluster);
        }

        return clusterList;
    }

    protected String getTenantConfigContent(String tenantDomain) throws RegistryException, UserStoreException {
        APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();

        return apimRegistryService
                .getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);
    }
}
