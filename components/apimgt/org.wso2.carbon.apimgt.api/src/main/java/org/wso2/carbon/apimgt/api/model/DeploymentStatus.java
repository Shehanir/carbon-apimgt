package org.wso2.carbon.apimgt.api.model;

import java.util.List;
import java.util.Map;

public class DeploymentStatus {
    //use this class to build the model
    private String clusterName;
    private Integer podsRunning;
    private  List<Map<String,String>> podStatus;

    public DeploymentStatus() {
    }

    public DeploymentStatus(String clusterName, Integer podsRunning, boolean deployed, List<Map<String, String>> podStatus) {
        this.clusterName = clusterName;
        this.podsRunning = podsRunning;
        this.podStatus = podStatus;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getPodsRunning() {
        return podsRunning;
    }

    public void setPodsRunning(Integer podsRunning) {
        this.podsRunning = podsRunning;
    }

    public List<Map<String, String>> getPodStatus() {
        return podStatus;
    }

    public void setPodStatus(List<Map<String, String>> podStatus) {
        this.podStatus = podStatus;
    }

}
