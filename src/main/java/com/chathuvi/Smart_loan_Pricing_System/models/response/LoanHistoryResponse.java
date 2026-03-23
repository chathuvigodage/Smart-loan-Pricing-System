package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanHistoryResponse {
    private List<LoanListResponse> loanListResponse;
    private String noOfApplications;
    private String avgAcceptance;
    private String avgRejection;
}
