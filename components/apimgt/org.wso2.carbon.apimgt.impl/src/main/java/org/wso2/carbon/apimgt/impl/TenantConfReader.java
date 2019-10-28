package org.wso2.carbon.apimgt.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TenantConfReader {

    private String k8sMasterURL;
    private String saToken;
    private String namespace;
    private String replicas;


    public Client readTenant(String tenantConf) throws IOException, ParseException {

        JSONParser jsonParser = new JSONParser();
        Object tenantObject = jsonParser.parse(tenantConf);
        JSONObject tenant_conf = (JSONObject) tenantObject;

        Client k8sClient = new Client();

        k8sClient.setMasterURL((String) tenant_conf.get("k8sMasterURL"));
        k8sClient.setSaToken((String) tenant_conf.get("saToken"));
        k8sClient.setNamespace((String) tenant_conf.get("namespace"));
        k8sClient.setReplicas(Math.toIntExact((long) tenant_conf.get("replicas")));

        return k8sClient;
    }
}
