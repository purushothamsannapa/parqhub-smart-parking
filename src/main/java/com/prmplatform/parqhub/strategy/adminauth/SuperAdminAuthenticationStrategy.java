package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Concrete strategy for Super Admin authentication
 */
public class SuperAdminAuthenticationStrategy implements AdminAuthenticationStrategy {
    
    @Override
    public boolean authenticate(Admin admin) {
        // Super Admin has access to all systems
        return admin != null && admin.getRole() == Admin.Role.SUPER_ADMIN;
    }
    
    @Override
    public String getRoleName() {
        return "Super Admin";
    }
}