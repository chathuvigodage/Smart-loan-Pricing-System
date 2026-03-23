package com.chathuvi.Smart_loan_Pricing_System.repository;

import com.chathuvi.Smart_loan_Pricing_System.entity.AppUser;
import com.chathuvi.Smart_loan_Pricing_System.entity.LoanData;
import com.chathuvi.Smart_loan_Pricing_System.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanDataRepository extends JpaRepository<LoanData, Long> {

    List<LoanData> findByCreatedBy(String createdBy);
    List<LoanData> findByCreatedByAndStatus(String createdBy, String status);
    List<LoanData> findByCreatedByAndCreatedAt(String createdBy, LocalDate createdAt);
}
