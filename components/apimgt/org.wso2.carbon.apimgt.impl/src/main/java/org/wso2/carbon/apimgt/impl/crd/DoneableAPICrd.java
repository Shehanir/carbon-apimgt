package org.wso2.carbon.apimgt.impl.crd;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableAPICrd extends CustomResourceDoneable<APICrd> {
    public DoneableAPICrd(APICrd resource, Function function) {
        super(resource, function);
    }
}
