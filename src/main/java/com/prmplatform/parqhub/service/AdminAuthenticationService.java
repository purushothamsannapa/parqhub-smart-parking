package com.prmplatform.parqhub.service;

import com.prmplatform.parqhub.model.Admin;
import com.prmplatform.parqhub.strategy.adminauth.*;
import org.springframework.stereotype.Service;

/**
 * Service class for admin authentication using strategy pattern
 */
@Service
public class AdminAuthenticationService {
    
    /**
     * Authenticate admin based on their role
     */
    public boolean authenticateAdmin(Admin admin) {
        if (admin == null) {
            return false;
        }
        
        AdminAuthenticationStrategy strategy = getStrategyForRole(admin.getRole());
        AdminAuthenticationContext context = new AdminAuthenticationContext(strategy);
        return context.authenticate(admin);
    }
    
    /**
     * Get the appropriate strategy for a given role
     */
    private AdminAuthenticationStrategy getStrategyForRole(Admin.Role role) {
        switch (role) {
            case OPERATIONS_MANAGER:
                return new OperationsManagerAuthenticationStrategy();
            case FINANCE_OFFICER:
                return new FinanceOfficerAuthenticationStrategy();
            case SECURITY_SUPERVISOR:
                return new SecuritySupervisorAuthenticationStrategy();
            case IT_SUPPORT:
                return new ITSupportAuthenticationStrategy();
            case CUSTOMER_SERVICE_OFFICER:
                return new CustomerServiceAuthenticationStrategy();
            case SUPER_ADMIN:
            default:
                return new SuperAdminAuthenticationStrategy();
        }
    }
    
    /**
     * Check if admin has access to a specific dashboard section
     */
    public boolean hasAccessToSection(Admin admin, String section) {
        if (admin == null) {
            return false;
        }
        
        // Super admin has access to all sections
        if (admin.getRole() == Admin.Role.SUPER_ADMIN) {
            return true;
        }
        
        // Check role-specific access
        switch (section.toLowerCase()) {
            case "operations":
                return admin.getRole() == Admin.Role.OPERATIONS_MANAGER;
            case "finance":
                return admin.getRole() == Admin.Role.FINANCE_OFFICER;
            case "security":
                return admin.getRole() == Admin.Role.SECURITY_SUPERVISOR;
            case "it":
                return admin.getRole() == Admin.Role.IT_SUPPORT;
            case "customer":
                return admin.getRole() == Admin.Role.CUSTOMER_SERVICE_OFFICER;
            default:
                return false;
        }
    }
}