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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMRegistryService;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CloudClustersInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MonetizationAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SecurityAuditAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class SettingsMappingUtil {


    private static final Log log = LogFactory.getLog(SettingsMappingUtil.class);

    /**
     * This method feeds data into the settingsDTO
     * @param isUserAvailable check if user is logged in
     * @return SettingsDTO
     * @throws APIManagementException
     */
    public SettingsDTO fromSettingstoDTO(Boolean isUserAvailable) throws APIManagementException {
        SettingsDTO settingsDTO = new SettingsDTO();
        EnvironmentListDTO environmentListDTO = new EnvironmentListDTO();
        if (isUserAvailable) {
            Map<String, Environment> environments = APIUtil.getEnvironments();
            if (environments != null) {
                environmentListDTO = EnvironmentMappingUtil.fromEnvironmentCollectionToDTO(environments.values());
            }
            //get kubernetes and istio cluster info
            settingsDTO.setCloudClustersInfo(getCloudClusterInfoFromTenantConf());
            settingsDTO.setEnvironment(environmentListDTO.getList());
            settingsDTO.setStoreUrl(APIUtil.getStoreUrl());
            settingsDTO.setMonetizationAttributes(getMonetizationAttributes());
            settingsDTO.setSecurityAuditProperties(getSecurityAuditProperties());
            settingsDTO.setExternalStoresEnabled(
                    APIUtil.isExternalStoresEnabled(RestApiUtil.getLoggedInUserTenantDomain()));
        }
        settingsDTO.setScopes(GetScopeList());
        return settingsDTO;
    }

    public Map<String, Environment> getCloudClusterInfo () throws APIManagementException{

        //get environment list from conf file
        Map<String, Environment> environments = APIUtil.getEnvironments();
        //Get cloud environments from tenant-conf.json file
        //Get tenant domain to access tenant conf
        APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();
//        String username = RestApiUtil.getLoggedInUsername();
//        MultitenantUtils.getTenantDomain(username);
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        //read tenant-conf.json and get details
        try {
            String getTenantDoimanConfContent = apimRegistryService
                    .getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);

            //parse tenant-conf to a json objectxx
            JSONParser jsonParser = new JSONParser();
            Object tenantObject = jsonParser.parse(getTenantDoimanConfContent);
            JSONObject tenant_conf = (JSONObject) tenantObject;

            //iterate tenant conf k8sClusterInfo JSONarray and add values to environment obj and add it to the environments list
            JSONArray k8sClusterInfo = (JSONArray) tenant_conf.get("K8sClusterInfo");
            for (int i = 0; i < k8sClusterInfo.size(); i++) {
                Environment env = new Environment();
                env.setName("Kubernetes");
                env.setServerURL((String) ((JSONObject) k8sClusterInfo.get(i)).get("k8sMasterURL"));
                env.setApiGatewayEndpoint("https://localhost:9095");

                environments.put("Kubernetes", env);
            }
            System.out.println("printing the full map");
            for (Map.Entry<String, Environment> entry : environments.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue().toString());
            }
        }  catch (RegistryException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (UserStoreException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (ParseException e) {
            handleException("Couldn't parse tenant configuration for reading extension handler position", e);
        }
        return environments;

    }

    /**
     * This method returns the scope list from the publisher-api.yaml
     * @return  List<String> scope list
     * @throws APIManagementException
     */
    private List<String> GetScopeList() throws APIManagementException {
        String definition = null;
        try {
            definition = IOUtils
                    .toString(RestApiUtil.class.getResourceAsStream("/publisher-api.yaml"), "UTF-8");
        } catch (IOException e) {
            log.error("Error while reading the swagger definition", e);
        }
        APIDefinition parser = OASParserUtil.getOASParser(definition);
        Set<Scope> scopeSet = parser.getScopes(definition);
        List<String> scopeList = new ArrayList<>();
        for (Scope entry : scopeSet) {
            scopeList.add(entry.getKey());
        }
        return scopeList;
    }

    /**
     * This method returns the monetization properties from configuration
     *
     * @return List<String> monetization properties
     * @throws APIManagementException
     */
    private List<MonetizationAttributeDTO> getMonetizationAttributes() {

        List<MonetizationAttributeDTO> monetizationAttributeDTOSList = new ArrayList<MonetizationAttributeDTO>();
        JSONArray monetizationAttributes = APIUtil.getMonetizationAttributes();

        for (int i = 0; i < monetizationAttributes.size(); i++) {
            JSONObject monetizationAttribute = (JSONObject) monetizationAttributes.get(i);
            MonetizationAttributeDTO monetizationAttributeDTO = new MonetizationAttributeDTO();
            monetizationAttributeDTO.setName((String) monetizationAttribute.get(APIConstants.Monetization.ATTRIBUTE));
            monetizationAttributeDTO.setDisplayName(
                    (String) monetizationAttribute.get(APIConstants.Monetization.ATTRIBUTE_DISPLAY_NAME));
            monetizationAttributeDTO.setDescription(
                    (String) monetizationAttribute.get(APIConstants.Monetization.ATTRIBUTE_DESCRIPTION));
            monetizationAttributeDTO
                    .setRequired((Boolean) monetizationAttribute.get(APIConstants.Monetization.IS_ATTRIBITE_REQUIRED));
            monetizationAttributeDTO
                    .setHidden((Boolean) monetizationAttribute.get(APIConstants.Monetization.IS_ATTRIBUTE_HIDDEN));
            monetizationAttributeDTOSList.add(monetizationAttributeDTO);
        }
        return monetizationAttributeDTOSList;
    }

    private List<CloudClustersInfoDTO> getCloudClusterInfoFromTenantConf() throws APIManagementException{
        List<CloudClustersInfoDTO> cloudClusterInfoDTOSList = new ArrayList<CloudClustersInfoDTO>();

        //Get cloud environments from tenant-conf.json file
        //Get tenant domain to access tenant conf
        APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();
//        String username = RestApiUtil.getLoggedInUsername();
//        MultitenantUtils.getTenantDomain(username);
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        //read tenant-conf.json and get details
        try {
            String getTenantDoimanConfContent = apimRegistryService
                    .getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);

            //parse tenant-conf to a json objectxx
            JSONParser jsonParser = new JSONParser();
            Object tenantObject = jsonParser.parse(getTenantDoimanConfContent);
            JSONObject tenant_conf = (JSONObject) tenantObject;

            //get kubernetes cluster info
            JSONObject k8sClusterInfo = (JSONObject) tenant_conf.get("containerManagementInfo");
            CloudClustersInfoDTO k8sClustersInfoDTO = new CloudClustersInfoDTO();
            k8sClustersInfoDTO.setName((String) k8sClusterInfo.get("className"));
            k8sClustersInfoDTO.setMasterUrl((String) k8sClusterInfo.get("k8sMasterURL"));
            k8sClustersInfoDTO.setNamespace((String) k8sClusterInfo.get("namespace"));
            cloudClusterInfoDTOSList.add(k8sClustersInfoDTO);

            //get istio cluster info
            JSONObject istioClusterInfo = (JSONObject) tenant_conf.get("serviceMeshInfo");
            CloudClustersInfoDTO istioClustersInfoDTO = new CloudClustersInfoDTO();
            istioClustersInfoDTO.setName((String) istioClusterInfo.get("className"));
            istioClustersInfoDTO.setMasterUrl((String) istioClusterInfo.get("masterURL"));
            istioClustersInfoDTO.setNamespace((String) istioClusterInfo.get("namespace"));
            cloudClusterInfoDTOSList.add(istioClustersInfoDTO);

        }  catch (RegistryException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (UserStoreException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (ParseException e) {
            handleException("Couldn't parse tenant configuration for reading extension handler position", e);
        }
        return cloudClusterInfoDTOSList;
    }

    /**
     * This method returns the Security Audit properties from the configuration
     *
     * @return SecurityAuditAttributeDTO Security Audit Attributes
     * @throws APIManagementException
     */
    private SecurityAuditAttributeDTO getSecurityAuditProperties() throws APIManagementException {
        SecurityAuditAttributeDTO properties = new SecurityAuditAttributeDTO();

        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiUtil.getProvider(username);

        JSONObject securityAuditPropertyObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
        if (securityAuditPropertyObject != null) {
            String apiToken = (String) securityAuditPropertyObject.get(APIConstants.SECURITY_AUDIT_API_TOKEN);
            String collectionId = (String) securityAuditPropertyObject.get(APIConstants.SECURITY_AUDIT_COLLECTION_ID);

            properties.setApiToken(apiToken);
            properties.setCollectionId(collectionId);
        }
        return properties;
    }
}
