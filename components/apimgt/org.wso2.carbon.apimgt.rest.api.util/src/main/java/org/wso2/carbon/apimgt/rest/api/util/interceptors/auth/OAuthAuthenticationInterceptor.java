/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.OauthAuthenticator;
import java.util.regex.Pattern;

/**
 * This class will validate incoming requests with OAUth authenticator headers
 * You can place this handler name in your web application if you need OAuth
 * based authentication.
 */
public class OAuthAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(OAuthAuthenticationInterceptor.class);
    private static final String OAUTH_AUTHENTICATOR = "OAuth";
    private static final String REGEX_BEARER_PATTERN = "Bearer\\s";
    private static final Pattern PATTERN = Pattern.compile(REGEX_BEARER_PATTERN);
    private volatile OauthAuthenticator authenticator;

    public OAuthAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }
    public void handleMessage(Message inMessage) {
        //by-passes the interceptor if user calls an anonymous api
        if (RestApiUtil.checkIfAnonymousAPI(inMessage)) {
            return;
        }

        //call JWT interceptor
        String consumerKey = java.util.UUID.randomUUID().toString();
        String generatedJWT = "";
        java.util.Map attributes = new java.util.HashMap();

        attributes.put("azp", consumerKey);
        attributes.put("aud", "choreoportalapplication");
        attributes.put("name", "test");
        attributes.put("email", "first@gmail.com");
        attributes.put("scope", "org-id-1:apim:api_create org-id-1:apim:api_publish org-id-2:apim:api_subscribe org-id-2:apim:api_view");
        java.io.File keyStoreFile = java.nio.file.Paths.get("/Users/shehanir/Documents/Choreo_features/JWT_Auth/test1/wso2am-4.0.0-SNAPSHOT/repository/resources/security/wso2carbon.jks").toFile();

        String ISSUER_1 = "https://localhost:9443/oauth2/token";
        try {
            generatedJWT = JWTAuthenticationInterceptor.generatedJWT(keyStoreFile, "wso2carbon", "wso2carbon", "wso2carbon", "wso2carbon", "userexternal",
                    ISSUER_1, attributes);

        } catch (Exception e){
            e.printStackTrace();
        }
        logger.info("Generated JWT token" + generatedJWT);

        //check if "Authorization: Bearer" header is present in the request. If not, by-passes the interceptor. If yes,
        //set the request_authentication_scheme property in the message as oauth2.
        String accessToken = RestApiUtil.extractOAuthAccessTokenFromMessage(inMessage,
                RestApiConstants.REGEX_BEARER_PATTERN, RestApiConstants.AUTH_HEADER_NAME);
        if (accessToken == null) {
            return;
        }
        //identify Oauth2 and JWT tokens seperately
        if (accessToken.contains(RestApiConstants.DOT)){
            inMessage.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.JWT_AUTHENTICATION);
            inMessage.put(RestApiConstants.AUTHENTICATION_CLASS, RestApiConstants.JWT_AUTHENTICATOR);
        } else {
            inMessage.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.OAUTH2_AUTHENTICATION);
            inMessage.put(RestApiConstants.AUTHENTICATION_CLASS, RestApiConstants.OAUTH2_AUTHENTICATOR);
        }

        if(handleRequest(inMessage, null)){
            /*String requestedTenant = ((ArrayList) ((TreeMap) (inMessage.get(Message.PROTOCOL_HEADERS))).get("X-WSO2_Tenant")).get(0).toString();
            if(requestedTenant!=null){
                RestApiUtil.setThreadLocalRequestedTenant(requestedTenant);
            }
            else {
                RestApiUtil.unsetThreadLocalRequestedTenant();
            }*/
            if(logger.isDebugEnabled()) {
                logger.debug("User logged into Web app using OAuth Authentication");
            }
        }
        else{
            throw new AuthenticationException("Unauthenticated request");
        }
    }

    /**
     * This method will initialize Web APP authenticator to validate incoming requests
     * Here we will get implementation class and create object of it.
     */
    public void initializeAuthenticator(String authenticationClass) throws APIManagementException {
        try {
            //TODO Retrieve this class name from configuration and let it configurable.
            //  authenticator = (WebAppAuthenticator) APIUtil.getClassForName(
            //      RestApiConstants.REST_API_WEB_APP_AUTHENTICATOR_IMPL_CLASS_NAME).newInstance();

            Class<OauthAuthenticator> WebAppAuthenticator = (Class<OauthAuthenticator>) Class.forName(authenticationClass);
            authenticator = WebAppAuthenticator.getDeclaredConstructor().newInstance();
//            if (authenticationScheme.equals(RestApiConstants.JWT_AUTHENTICATION)){
//                authenticator = new
//            } else {
//                authenticator = new WebAppAuthenticatorImpl();
//            }
        } catch (Exception e) {
            throw new APIManagementException("Error while initializing authenticator of " + "type: ",e);
        }

    }

    /**
     * authenticate requests received at the REST API endpoint, using HTTP OAuth headers as the authentication
     * mechanism. This method returns a null value which indicates that the request to be processed.
     */
    public boolean handleRequest(Message message, ClassResourceInfo resourceInfo) {

//        if (authenticator == null) {
            try {
                initializeAuthenticator(message.get(RestApiConstants.AUTHENTICATION_CLASS).toString());
            } catch (APIManagementException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(" Initializing the authenticator resulted in an exception", e);
                }else{
                    logger.error(e.getMessage());
                }
                return false;
            }
//          }
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Authenticating request: " + message.getId()));
                }
                if (authenticator.authenticate(message)) {
                    return true;
                }
            } catch (APIManagementException e) {
                logger.error("Error while authenticating incoming request to API Manager REST API", e);
            }
        return false;
    }


}