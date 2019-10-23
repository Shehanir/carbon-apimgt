package org.wso2.carbon.apimgt.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TenantConfReader {

    private static String k8sMasterURL;
    private static String saToken;
    private static String namespace;


    public void readTenant() throws IOException, ParseException {

        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader("/repository/resources/tenant-conf.json");
        Object tenantObject = jsonParser.parse(fileReader);
        JSONObject tenant_conf = (JSONObject) tenantObject;

        this.k8sMasterURL = (String) tenant_conf.get("k8sMasterURL");
        this.saToken = (String) tenant_conf.get("saToken");
        this.namespace = (String) tenant_conf.get("namespace");

    }

    public ArrayList<String > getSecrets() {

        ArrayList<String> secrets = new ArrayList<String>();
        secrets.add(0,k8sMasterURL);
        secrets.add(1,saToken);
        secrets.add(2,namespace);
        return secrets;
    }
}
