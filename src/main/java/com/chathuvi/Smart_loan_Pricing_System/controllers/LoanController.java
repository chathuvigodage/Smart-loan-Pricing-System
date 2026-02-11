package com.chathuvi.Smart_loan_Pricing_System.controllers;

import com.chathuvi.Smart_loan_Pricing_System.models.request.FeedbackRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.LoanDetailRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.DefaultResponse;
import com.chathuvi.Smart_loan_Pricing_System.services.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loan")
@Slf4j
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/send-details")
    public DefaultResponse sendLoanDetails(@RequestBody LoanDetailRequest request) {
        DefaultResponse response = loanService.sendLoanDataToModel(request);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(),response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

    @PostMapping("/feedback")
    public DefaultResponse sendFeedback(@RequestBody FeedbackRequest request) {
        DefaultResponse response = loanService.sendFeedback(request);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(),response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

}
