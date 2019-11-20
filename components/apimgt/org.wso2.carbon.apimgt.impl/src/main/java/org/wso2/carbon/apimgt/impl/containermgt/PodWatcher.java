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

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PodWatcher {

    private static final Logger log = LoggerFactory.getLogger(PodWatcher.class);
    private PodList podList;

    public void setPodList(PodList podList) {
        this.podList = podList;
    }

    public JSONObject getPodStatus() {
        JSONObject podJsonObject = new JSONObject();
        JSONArray podJsonArray = new JSONArray();
        List<Pod> pods = podList.getItems();
        int podSize = pods.size();
        for (int i = 0; i < podSize; i++) {

            JSONObject singlePodJsonObject = new JSONObject();
            String podName = pods.get(i).getMetadata().getName();
            String podStatus = pods.get(i).getStatus().getPhase();
            List<ContainerStatus> containerStatusList = pods.get(i).getStatus().getContainerStatuses();

            JSONObject containers = getContainerStatus(containerStatusList);
            singlePodJsonObject.put("name", podName);
            singlePodJsonObject.put("status", podStatus);
            singlePodJsonObject.put("containers", containers);
            podJsonArray.add(singlePodJsonObject);
        }
        podJsonObject.put("size", podSize);
        podJsonObject.put("pods", podJsonArray);

        //pods.get(0).getStatus().getConditions().get(0).getStatus(); // Check This
        return podJsonObject;
    }

    private JSONObject getContainerStatus(List<ContainerStatus> containerStatuses) {

        JSONObject containersJsonObject = new JSONObject();
        JSONArray containerStatusJsonArray = new JSONArray();
        int numContainers = containerStatuses.size();

        for (int i = 0; i < numContainers; i++) {
            JSONObject singleContainerJsonObject = new JSONObject();
            String containerName = containerStatuses.get(i).getName();
            boolean containerReady = containerStatuses.get(i).getReady();  //If the container is ready this will be true
            singleContainerJsonObject.put(containerName, containerReady);
            containerStatusJsonArray.add(singleContainerJsonObject);

        }
        containersJsonObject.put("size", numContainers);
        containersJsonObject.put("names", containerStatusJsonArray);
        return containersJsonObject;
    }
}
