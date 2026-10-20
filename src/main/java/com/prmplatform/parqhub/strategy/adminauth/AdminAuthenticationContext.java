package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Context class for admin authentication strategies
 */
public class AdminAuthenticationContext {
    private AdminAuthenticationStrategy authenticationStrategy;
    
    public AdminAuthenticationContext(AdminAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }
    
    public void setAuthenticationStrategy(AdminAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }
    
    public boolean authenticate(Admin admin) {
        return authenticationStrategy.authenticate(admin);
    }
    
    public String getRoleName() {
        return authenticationStrategy.getRoleName();
    }
}