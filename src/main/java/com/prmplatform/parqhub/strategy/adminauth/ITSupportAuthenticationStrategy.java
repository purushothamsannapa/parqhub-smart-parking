package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Concrete strategy for IT Support authentication
 */
public class ITSupportAuthenticationStrategy implements AdminAuthenticationStrategy {
    
    @Override
    public boolean authenticate(Admin admin) {
        // IT Support can access system-related data
        return admin != null && 
               (admin.getRole() == Admin.Role.IT_SUPPORT || 
                admin.getRole() == Admin.Role.SUPER_ADMIN);
    }
    
    @Override
    public String getRoleName() {
        return "IT Support";
    }
}