package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);
    long count();

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.contactNo) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrContactNoContainingIgnoreCase(@Param("search") String search, @Param("search") String search2, @Param("search") String search3, Pageable pageable);

}