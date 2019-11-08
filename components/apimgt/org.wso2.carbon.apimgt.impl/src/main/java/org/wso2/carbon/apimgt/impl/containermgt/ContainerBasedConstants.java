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
}
