package com.bestsearch.bestsearchservice.auth;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

public class AuthFilter extends GenericFilterBean {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    var principal = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    String internalId = null;
    if (principal != null) {
      internalId = ((Jwt) principal.getPrincipal()).getClaim("custom:i_id").toString();
      principal.setDetails(new UserAdditionalInfo(internalId));

    }
    filterChain.doFilter(request, response);
  }
}
