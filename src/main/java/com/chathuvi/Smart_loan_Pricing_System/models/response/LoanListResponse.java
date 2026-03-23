package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanListResponse {
    private String loanId;
    private String customerName;
    private String createdAt;
    private String offeredRate;
    private String confidence;
    private String status;

}
