package com.bestsearch.bestsearchservice.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {
  

  @PreAuthorize("hasAuthority('admin')")
  @GetMapping("/hello")
  public String sayHello() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentPrincipalName = authentication.getName();
    UserAdditionalInfo userAdditionalInfo = (UserAdditionalInfo) authentication.getDetails();
    return "Hello " + userAdditionalInfo.getInternalId();
  }
}
