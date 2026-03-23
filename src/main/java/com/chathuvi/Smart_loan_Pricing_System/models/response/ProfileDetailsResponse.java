package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileDetailsResponse {
    private String username;
    private String email;
    private String role;
}
