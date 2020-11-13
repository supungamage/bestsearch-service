package com.bestsearch.bestsearchservice.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

  @PreAuthorize("hasAuthority('admin')")
  @GetMapping("/hello")
  public String sayHello(){
    return "Hello";
  }
}
