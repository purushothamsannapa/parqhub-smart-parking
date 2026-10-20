package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Concrete strategy for Finance Officer authentication
 */
public class FinanceOfficerAuthenticationStrategy implements AdminAuthenticationStrategy {
    
    @Override
    public boolean authenticate(Admin admin) {
        // Finance Officer can access financial data
        return admin != null && 
               (admin.getRole() == Admin.Role.FINANCE_OFFICER || 
                admin.getRole() == Admin.Role.SUPER_ADMIN);
    }
    
    @Override
    public String getRoleName() {
        return "Finance Officer";
    }
}