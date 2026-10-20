package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Concrete strategy for Operations Manager authentication
 */
public class OperationsManagerAuthenticationStrategy implements AdminAuthenticationStrategy {
    
    @Override
    public boolean authenticate(Admin admin) {
        // Operations Manager can access operational data
        return admin != null && 
               (admin.getRole() == Admin.Role.OPERATIONS_MANAGER || 
                admin.getRole() == Admin.Role.SUPER_ADMIN);
    }
    
    @Override
    public String getRoleName() {
        return "Operations Manager";
    }
}