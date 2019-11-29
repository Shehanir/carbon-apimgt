package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class CloudClustersInfoDTO   {
  
    private String name = null;
    private String namespace = null;
    private String masterUrl = null;

  /**
   **/
  public CloudClustersInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Kubernetes", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public CloudClustersInfoDTO namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  
  @ApiModelProperty(example = "default", required = true, value = "")
  @JsonProperty("namespace")
  @NotNull
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   **/
  public CloudClustersInfoDTO masterUrl(String masterUrl) {
    this.masterUrl = masterUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9095", required = true, value = "")
  @JsonProperty("masterUrl")
  @NotNull
  public String getMasterUrl() {
    return masterUrl;
  }
  public void setMasterUrl(String masterUrl) {
    this.masterUrl = masterUrl;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CloudClustersInfoDTO cloudClustersInfo = (CloudClustersInfoDTO) o;
    return Objects.equals(name, cloudClustersInfo.name) &&
        Objects.equals(namespace, cloudClustersInfo.namespace) &&
        Objects.equals(masterUrl, cloudClustersInfo.masterUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, namespace, masterUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CloudClustersInfoDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    masterUrl: ").append(toIndentedString(masterUrl)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

