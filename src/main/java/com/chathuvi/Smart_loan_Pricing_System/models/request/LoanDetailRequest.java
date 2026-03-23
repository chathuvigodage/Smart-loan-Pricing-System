package com.chathuvi.Smart_loan_Pricing_System.models.request;

import lombok.Getter;

import java.util.List;

@Getter
public class LoanDetailRequest {
    private String name;
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
    private List<Double> interestRates;
}
