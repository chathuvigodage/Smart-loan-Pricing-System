package com.chathuvi.Smart_loan_Pricing_System.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoanDataResponse {

    private String rate;
    @JsonProperty("probability_rate")
    private String probabilityRate;
    private String profit;
    private String status = "Alternative";
}
