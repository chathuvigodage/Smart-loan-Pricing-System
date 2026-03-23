package com.chathuvi.Smart_loan_Pricing_System.repository;

import com.chathuvi.Smart_loan_Pricing_System.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
