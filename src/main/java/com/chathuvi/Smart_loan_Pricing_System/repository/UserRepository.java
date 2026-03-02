package com.chathuvi.Smart_loan_Pricing_System.repository;

import com.chathuvi.Smart_loan_Pricing_System.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

}
