package com.chathuvi.Smart_loan_Pricing_System.models.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDataRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;
}
