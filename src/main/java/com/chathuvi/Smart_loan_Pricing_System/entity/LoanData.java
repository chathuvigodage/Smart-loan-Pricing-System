package com.chathuvi.Smart_loan_Pricing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanData {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String createdBy;
    private String updatedBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    private String customerName;
    private Double loanAmount;
    private Double loanDuration;
    private Double totalDebtToIncomeRatio;
    private Double creditScore;
    private Integer numberOfOpenCreditLines;

    // Financial background
    private Double annualIncome;
    private Double savingsAccountBalance;
    private Double totalLiabilities;

    // Socioeconomic background
    private Integer age;
    private String educationLevel;
    private String maritalStatus;
    private String employmentStatus;

    // Bank relationship
    private Double paymentHistory;
    private Double acceptanceProb;
    private Double expectedProfit;
    private Double selectedRate;
    private String contextId;
    private String status;
    private String reason;
}
