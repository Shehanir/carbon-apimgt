package org.wso2.carbon.apimgt.impl.crd;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class APICrd extends CustomResource {

    private APICrdSpec spec;

    public APICrdSpec getSpec() {
        return spec;
    }

    public void setSpec(APICrdSpec spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        return "APICrd{" +
                "apiVersion='" + getApiVersion() + "'" +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                "}";
    }

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

}
