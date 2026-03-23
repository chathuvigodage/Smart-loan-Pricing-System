package com.chathuvi.Smart_loan_Pricing_System.models.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
   private String username;
   private String email;
   private String role;
   private String password;
}
