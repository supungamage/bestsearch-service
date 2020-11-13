package com.bestsearch.bestsearchservice.share.audit;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
    /*
      TODO: this has to be replaced with logged user
    */
        return Optional.of("System");
    }

}
