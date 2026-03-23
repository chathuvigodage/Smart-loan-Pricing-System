package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecentApplicationResponse {
    private long applicationId;
    private String status;
    private Double amount;
    private LocalDate date;
}
