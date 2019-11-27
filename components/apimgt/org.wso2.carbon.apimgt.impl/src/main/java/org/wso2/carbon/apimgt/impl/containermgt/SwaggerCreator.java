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

package org.wso2.carbon.apimgt.impl.containermgt;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;

import java.util.*;

/**
 * This class extends the OAS3Parser class in order to override its method
 * "getOASDefinitionForPublisher".
 */
public class SwaggerCreator extends OAS3Parser {

    private static final Log log = LogFactory.getLog(SwaggerCreator.class);
    private static final String OPENAPI_SECURITY_SCHEMA_KEY = "default";
    private boolean securityOauth2 = false;
    private boolean securityBasicAuth = false;

    public boolean isSecurityOauth2() {
        return securityOauth2;
    }

    public boolean isSecurityBasicAuth() {
        return securityBasicAuth;
    }

    /**
     * This method returns the swagger definition of an api
     * which suits for k8s_apim_operator
     *
     * @param api               API
     * @param oasDefinition     Open API definition
     * @param basicSecurityName Security CR name for basic-auth
     * @param jwtSecurityName   Security CR name for jwt
     * @param oauthSecurityName Security CR name for OAuth2
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     * @throws ParseException         throws if the oasDefinition is not in json format
     */


    public String getOASDefinitionForPublisher(API api, String oasDefinition, String basicSecurityName,
                                               String jwtSecurityName, String oauthSecurityName)
            throws APIManagementException, ParseException {

        OpenAPI openAPI = getOpenAPI(oasDefinition);
        if (openAPI.getComponents() == null) {
            openAPI.setComponents(new Components());
        }
        Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
        if (securitySchemes == null) {
            securitySchemes = new HashMap<>();
            openAPI.getComponents().setSecuritySchemes(securitySchemes);
        }
        SecurityScheme securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
        if (securityScheme == null) {
            securityScheme = new SecurityScheme();
            securityScheme.setType(SecurityScheme.Type.OAUTH2);
            securitySchemes.put(OPENAPI_SECURITY_SCHEMA_KEY, securityScheme);
        }
        if (securityScheme.getFlows() == null) {
            securityScheme.setFlows(new OAuthFlows());
        }
        // setting scopes id if it is null
        // https://github.com/swagger-api/swagger-parser/issues/1202
        OAuthFlow oAuthFlow = securityScheme.getFlows().getImplicit();
        if (oAuthFlow == null) {
            oAuthFlow = new OAuthFlow();
            securityScheme.getFlows().setImplicit(oAuthFlow);
        }
        if (oAuthFlow.getScopes() == null) {
            oAuthFlow.setScopes(new Scopes());
        }

        if (api.getAuthorizationHeader() != null) {
            openAPI.addExtension(APIConstants.X_WSO2_AUTH_HEADER, api.getAuthorizationHeader());
        }
        if (api.getApiLevelPolicy() != null) {
            openAPI.addExtension(APIConstants.X_THROTTLING_TIER, api.getApiLevelPolicy());
        }
        openAPI.addExtension(APIConstants.X_WSO2_CORS, api.getCorsConfiguration());
        Object prodEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, true);
        if (prodEndpointObj != null) {
            openAPI.addExtension(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS, prodEndpointObj);
        }
        Object sandEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, false);
        if (sandEndpointObj != null) {
            openAPI.addExtension(APIConstants.X_WSO2_SANDBOX_ENDPOINTS, sandEndpointObj);
        }
        openAPI.addExtension(APIConstants.X_WSO2_BASEPATH, api.getContext());
        if (api.getTransports() != null) {
            openAPI.addExtension(APIConstants.X_WSO2_TRANSPORTS, api.getTransports().split(","));
        }

        String apiDefinition = Json.pretty(openAPI);
        JSONParser jsonParser = new JSONParser();
        JSONObject apiDefinitionJsonObject = (JSONObject) jsonParser.parse(apiDefinition);

        /**
         * Removing the "security" key from the JSONObject
         */
        apiDefinitionJsonObject.remove("security");
        ((JSONObject) ((JSONObject) apiDefinitionJsonObject.get("components")).get("securitySchemes")).remove("default");
        Set<String> paths = ((JSONObject) apiDefinitionJsonObject.get("paths")).keySet();
        Iterator iterator = paths.iterator();

        /**
         * Removing the "security" attribute from each RESTAPI verb of each path in the swagger
         */
        while (iterator.hasNext()) {
            String path = (String) iterator.next();
            Set verbs = ((JSONObject) ((JSONObject) apiDefinitionJsonObject.get("paths")).get(path)).keySet();
            Iterator verbIterator = verbs.iterator();
            while (verbIterator.hasNext()) {
                String verb = (String) verbIterator.next();
                ((JSONObject) ((JSONObject) ((JSONObject) apiDefinitionJsonObject.get("paths")).
                        get(path)).get(verb)).remove("security");
            }
        }

        String securityType = api.getApiSecurity().replace("oauth_basic_auth_api_key_mandatory", "");
        Boolean securityTypeOauth2 = isAPISecurityTypeOauth2(securityType);
        Boolean securityTypeBasicAuth = isAPISecurityBasicAuth(securityType);


        if (securityTypeBasicAuth && !securityTypeOauth2 && !basicSecurityName.equals("")) {

            SecurityRequirement basicOauthSecurityReq = referBasicAuthInSwagger(basicSecurityName);
            List<SecurityRequirement> basicAuth = new ArrayList<SecurityRequirement>();
            basicAuth.add(basicOauthSecurityReq);
            apiDefinitionJsonObject.put("security", basicAuth);
        } else if (securityTypeOauth2 && !securityTypeBasicAuth) {

            if (!oauthSecurityName.equals("") || !jwtSecurityName.equals("")) {

                SecurityRequirement oauth2SecurityReq = referOauth2InSwagger(oauthSecurityName, jwtSecurityName);
                List<SecurityRequirement> oauth2 = new ArrayList<SecurityRequirement>();
                oauth2.add(oauth2SecurityReq);
                apiDefinitionJsonObject.put("security", oauth2);
            }
        } else if (securityTypeBasicAuth && securityTypeOauth2) {

            if (!oauthSecurityName.equals("") || !basicSecurityName.equals("") || !jwtSecurityName.equals("")) {
                List<SecurityRequirement> basicOauthJWT = new ArrayList<SecurityRequirement>();
                SecurityRequirement basicOauthJWTSecurityReq = referBasicOAuth2JWTInSwagger(basicSecurityName,
                        oauthSecurityName, jwtSecurityName);

                basicOauthJWT.add(basicOauthJWTSecurityReq);
                apiDefinitionJsonObject.put("security", basicOauthJWT);
            }
        }

        return Json.pretty(apiDefinitionJsonObject);
    }

    /**
     * Get parsed OpenAPI object
     *
     * @param oasDefinition OAS definition
     * @return OpenAPI
     */
    private OpenAPI getOpenAPI(String oasDefinition) {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(oasDefinition, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        return parseAttemptForV3.getOpenAPI();
    }

    private Boolean isAPISecurityTypeOauth2(String apiSecurity) {
        if (apiSecurity.contains("oauth2")) {
            this.securityOauth2 = true;
            return true;
        }
        return false;
    }

    private Boolean isAPISecurityBasicAuth(String apiSecurity) {
        if (apiSecurity.contains("basic_auth")) {
            this.securityBasicAuth = true;
            return true;
        }
        return false;
    }

    private SecurityRequirement referOauth2InSwagger(String oauthSecurityName, String jwtSecurityName) {

        SecurityRequirement securityRequirement = new SecurityRequirement();

        if (!oauthSecurityName.equals("") && !jwtSecurityName.equals("")) {

            securityRequirement.addList(oauthSecurityName, new ArrayList<String>());
            securityRequirement.addList(jwtSecurityName, new ArrayList<String>());
        } else if (!oauthSecurityName.equals("") && jwtSecurityName.equals("")) {

            securityRequirement.addList(oauthSecurityName, new ArrayList<String>());
        } else if (oauthSecurityName.equals("") && !jwtSecurityName.equals("")) {

            securityRequirement.addList(jwtSecurityName, new ArrayList<String>());
        }

        return securityRequirement;
    }

    private SecurityRequirement referBasicAuthInSwagger(String basicSecurityName) {


        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList(basicSecurityName, new ArrayList<String>());

        return securityRequirement;
    }

    private SecurityRequirement referBasicOAuth2JWTInSwagger(String basicSecurityName, String oauthSecurityName,
                                                             String jwtSecurityName) {

        SecurityRequirement securityRequirement = referOauth2InSwagger(oauthSecurityName, jwtSecurityName);

        if (!basicSecurityName.equals("")) {
            securityRequirement.addList(basicSecurityName, new ArrayList<String>());
        }

        return securityRequirement;
    }

}
