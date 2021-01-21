package com.bestsearch.bestsearchservice.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class UserAdditionalInfo {

  public UserAdditionalInfo(String internalId) {
    this.internalId = internalId;
  }

  public UserAdditionalInfo() {
  }

  private String internalId;
}
