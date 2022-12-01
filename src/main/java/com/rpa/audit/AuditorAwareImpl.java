package com.rpa.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<String>{

	@Override
    public String getCurrentAuditor() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
