package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanApplicationResponse {
    private String loanApplicationId;
    private String customerName;
    private String createdData;
    private String selectedRate;
    private String acceptProb;
    private String status;
}
