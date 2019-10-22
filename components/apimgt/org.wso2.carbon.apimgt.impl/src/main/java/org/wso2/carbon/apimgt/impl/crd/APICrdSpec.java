package org.wso2.carbon.apimgt.impl.crd;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class APICrdSpec implements KubernetesResource{

    private int replicas;
    private String mode;
    private Definition definition;

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "APICrdSpec{" +
                "replicas='" + replicas + "'" +
                ", mode=" + mode +
                ", definition=" + definition +
                "}";
    }
}
