package com.chathuvi.Smart_loan_Pricing_System.controllers;

import com.chathuvi.Smart_loan_Pricing_System.models.request.ApplicationDataRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.FeedbackRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.LoanDetailRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.DefaultResponse;
import com.chathuvi.Smart_loan_Pricing_System.services.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan")
@Slf4j
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/send-details")
    public DefaultResponse sendLoanDetails(@RequestBody LoanDetailRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("#################################################################");
        DefaultResponse response = loanService.sendLoanDataToModel(request, userDetails);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(),response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

    @PostMapping("/feedback")
    public DefaultResponse sendFeedback(@RequestBody FeedbackRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        DefaultResponse response = loanService.sendFeedback(request, userDetails);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", "Status updated successfully",response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

   @GetMapping("/history")
   public DefaultResponse getLoanApplication(@AuthenticationPrincipal UserDetails userDetails){
       DefaultResponse response = loanService.getListOfApplications(userDetails);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(),response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

    @PostMapping("/specific/id")
    public DefaultResponse getLoanApplication(@Valid @RequestBody ApplicationDataRequest request){
        DefaultResponse response = loanService.getLoanSpecificDetails(request.getApplicationId());
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(),response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }
}
