package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.Admin;
import com.prmplatform.parqhub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmailAndPassword(String email, String password);

    @Query("SELECT a FROM Admin a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Admin> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(@Param("search") String search, @Param("search") String search2, Pageable pageable);
}
