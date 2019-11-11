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

/**
 * This class represents the
 * constants that are used for private-jet mode implementation
 */
public final class ContainerBasedConstants {

    public static final String PRIVATE_JET_MODE_LIST_ITEM = "Publish in private-jet mode";
    public static final String API_CRD_GROUP = "wso2.com";
    public static final String API_CRD_NAME = "apis." + API_CRD_GROUP;
    public static final String K8_CRD_VERSION = "apiextensions.k8s.io/v1beta1";
    public static final String API_CRD_VERSION = "v1alpha1";
    public static final String CRD_KIND = "API";
    public static final String CRD_KIND_SHORT = "api";
    public static final String CRD_KIND_PLURAL = "apis";
    public static final String API_CRD_SCOPE = "Namespaced";
    public static final String SWAGGER = "swagger";
    public static final String MODE = "privateJet";
    public static final String OAUTH_TYPE = "Oauth";
    public static final String SECURITY_CERTIFICATE = "wso2am300-secret";
    public static final String OAUTH2_END_POINT = "https://wso2apim.wso2:32001";
    public static final String OAUTH2_CREDENTIALS_NAME = "oauth-credentials";
    public static final String API_VERSION = API_CRD_GROUP + "/" + API_CRD_VERSION;
    public static final String SERVER_PEM = "MIIDfTCCAmWgAwIBAgIEbfVjBzANBgkqhkiG9w0BAQsFADBkMQswCQYDVQQGEwJVUz" +
            "ELMAkGA1UECBMCQ0ExFjAUBgNVBAcTDU1vdW50YWluIFZpZXcxDTALBgNVBAoTBFdTTzIxDTALBgNVBAsTBFdTTzIxEjAQBgNV" +
            "BAMTCWxvY2FsaG9zdDAeFw0xOTA4MjMxMjUwMzNaFw0yOTA4MjAxMjUwMzNaMGQxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQT" +
            "EWMBQGA1UEBxMNTW91bnRhaW4gVmlldzENMAsGA1UEChMEV1NPMjENMAsGA1UECxMEV1NPMjESMBAGA1UEAxMJbG9jYWxob3N0" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj3mYMT8N2SR8cpimdMOTpk/M8fOxPF1BHQAiCtld4nbksILgJKsA34" +
            "GSP5Oh4gLW21VCEPPzdGLnqfwM6ZoG/X0rcK5++VbqH/vH4Cclba6fqlLxCvTiRbPJ58Pe7+biCeQ368dG2aeBPV3EhO8br3Z/" +
            "LcQXASmhSWps8J3GOSx/49xzkHh59J2gJHhnvjxcszZAF35SLAb6F+2rJQrOJs6u7WfJv4NQxSyhcgcr4/+77JzNFEVUj4TPSB" +
            "y2WGAgK5ttP5+kG3+rKs0lQjTo9h/hK89KjbbRvoqZdpxnwQYxFDOk0CxijZVO5Cs3cabeUHZeXehHSgXj6W+VGMiDgwIDAQAB" +
            "ozcwNTAUBgNVHREEDTALgglsb2NhbGhvc3QwHQYDVR0OBBYEFFU2A4pBuR0aKGrgQAtrSlqWrNLLMA0GCSqGSIb3DQEBCwUAA4" +
            "IBAQAX+F30hIwI+8hO9IQ9Wr40+zL6KTgDxWraB450D7UyZ/FApKK2R/vYvIqab+H6u9XNCz63+sYgX6/UBSYW47ow6QMcv71x" +
            "epXbwtLqq3MQr6frgP52Z2jyQtAbDpirh4/IXkhF+S8DsDFxmlPy423LKnTqCqIfyv7Y8Y8lty5BWyfYJV7V2RJnZ4zIKv66U3" +
            "exxugR0WRGWy56nIY8GGaroxuC9cH6NkVwN9GmYoCa9PUGynQ4NHjeg6VSwQZ279VGpLhogWS67x8V/nR+yjI+qTjjCbJqsoHV" +
            "QL90Vxa+ASD1DViBA8ar1/9Ns5vIEZet5GT1nM10ZzEK+E1QMGed";
    public static final String OPAQUE = "Opaque";
    public static final String V1 = "v1";
    public static final String ADMIN64 = "YWRtaW4=";
}
