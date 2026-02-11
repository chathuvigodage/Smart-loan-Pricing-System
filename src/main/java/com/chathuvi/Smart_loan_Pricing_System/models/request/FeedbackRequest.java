package com.chathuvi.Smart_loan_Pricing_System.models.request;

import lombok.Getter;

@Getter
public class FeedbackRequest {
    private String contextId;
    private Double rate;
    private Boolean isAccepted;
    private Double profit;
}
