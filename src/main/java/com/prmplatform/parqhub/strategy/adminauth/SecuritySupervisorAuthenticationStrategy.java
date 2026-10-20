package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Concrete strategy for Security Supervisor authentication
 */
public class SecuritySupervisorAuthenticationStrategy implements AdminAuthenticationStrategy {
    
    @Override
    public boolean authenticate(Admin admin) {
        // Security Supervisor can access security-related data
        return admin != null && 
               (admin.getRole() == Admin.Role.SECURITY_SUPERVISOR || 
                admin.getRole() == Admin.Role.SUPER_ADMIN);
    }
    
    @Override
    public String getRoleName() {
        return "Security Supervisor";
    }
}