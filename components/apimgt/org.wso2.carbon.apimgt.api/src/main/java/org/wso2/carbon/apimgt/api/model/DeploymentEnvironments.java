package org.wso2.carbon.apimgt.api.model;

import java.util.List;
import java.util.Map;

public class DeploymentEnvironments {
    //use this class to build the model for selected deployment environments
    private String type;
    private List<String> clusterNames;

    public DeploymentEnvironments(String type, List<String> clusterNames) {
        this.type = type;
        this.clusterNames = clusterNames;
    }

    public DeploymentEnvironments() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getClusterNames() {
        return clusterNames;
    }

    public void setClusterNames(List<String> clusterNames) {
        this.clusterNames = clusterNames;
    }
}
