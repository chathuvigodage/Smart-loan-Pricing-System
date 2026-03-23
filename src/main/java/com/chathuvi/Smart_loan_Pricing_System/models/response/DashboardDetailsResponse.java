package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class DashboardDetailsResponse {
    private int noOfApplications;
    private int approvedApplications;
    private int rejectedApplications;
    private double totalLoanAmount;
    private List<RecentApplicationResponse> recentApplications;
}
