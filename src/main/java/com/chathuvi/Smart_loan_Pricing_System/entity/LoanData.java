package com.chathuvi.Smart_loan_Pricing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @ElementCollection
    private List<Double> interestRates;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<Double, Double> acceptanceProbs;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<Double, Double> expectedProfits;
    private Double selectedRate;
    private String contextId;
    private String status;
}
