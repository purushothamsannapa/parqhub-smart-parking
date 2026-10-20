package com.prmplatform.parqhub.strategy.adminauth;

import com.prmplatform.parqhub.model.Admin;

/**
 * Strategy interface for admin authentication based on roles
 */
public interface AdminAuthenticationStrategy {
    boolean authenticate(Admin admin);
    String getRoleName();
}