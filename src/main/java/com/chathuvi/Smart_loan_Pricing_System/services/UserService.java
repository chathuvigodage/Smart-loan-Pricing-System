package com.chathuvi.Smart_loan_Pricing_System.services;

import com.chathuvi.Smart_loan_Pricing_System.entity.AppUser;
import com.chathuvi.Smart_loan_Pricing_System.models.request.RegisterRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.DefaultResponse;
import com.chathuvi.Smart_loan_Pricing_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final  UserRepository userRepository;

    public DefaultResponse saveData(RegisterRequest request){
        try {
            AppUser appUser = AppUser.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .role(request.getRole())
                    .password(request.getPassword())
                    .build();

            userRepository.save(appUser);
            log.info("User saved successfully");
            return new DefaultResponse("200", "Success", "User Registered Successfully");
        } catch (Exception e) {
             log.error("Error is getting while saving user {}", e.getMessage());
             return new DefaultResponse("400", "Failed", "User already existed from the given email");
        }
    }
}
