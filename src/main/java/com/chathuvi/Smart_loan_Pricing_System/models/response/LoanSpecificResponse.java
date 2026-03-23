package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanSpecificResponse {
    private String applicationId;
    private String customerName;
    private String loanAmount;
    private String term;
    private String interestRate;
    private String monthlyPayment;
}
