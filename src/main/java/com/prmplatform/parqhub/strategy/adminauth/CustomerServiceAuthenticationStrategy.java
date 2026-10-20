package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Concrete strategy for Customer Service Officer authentication
 */
public class CustomerServiceAuthenticationStrategy implements AdminAuthenticationStrategy {
    
    @Override
    public boolean authenticate(Admin admin) {
        // Customer Service Officer can access customer-related data
        return admin != null && 
               (admin.getRole() == Admin.Role.CUSTOMER_SERVICE_OFFICER || 
                admin.getRole() == Admin.Role.SUPER_ADMIN);
    }
    
    @Override
    public String getRoleName() {
        return "Customer Service Officer";
    }
}