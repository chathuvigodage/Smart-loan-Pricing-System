package com.chathuvi.Smart_loan_Pricing_System.services;

import com.chathuvi.Smart_loan_Pricing_System.entity.AppUser;
import com.chathuvi.Smart_loan_Pricing_System.entity.LoanData;
import com.chathuvi.Smart_loan_Pricing_System.entity.Role;
import com.chathuvi.Smart_loan_Pricing_System.enums.LoanStatus;
import com.chathuvi.Smart_loan_Pricing_System.models.request.LoginRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.ProfileDetailsRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.RegisterRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.*;
import com.chathuvi.Smart_loan_Pricing_System.repository.LoanDataRepository;
import com.chathuvi.Smart_loan_Pricing_System.repository.UserRepository;
import com.chathuvi.Smart_loan_Pricing_System.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoanDataRepository loanDataRepository;


    public DefaultResponse saveData(RegisterRequest request){
        try {
            if(userRepository.existsByUsername(request.getUsername())){
                throw new RuntimeException("Username already taken");
            }

            if(userRepository.existsByEmail(request.getEmail())){
                throw new RuntimeException("Email already registered");
            }
            AppUser appUser = AppUser.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .role(Role.USER)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            userRepository.save(appUser);
            log.info("User saved successfully");

            String token = jwtService.generateToken(
                    new org.springframework.security.core.userdetails.User(
                            appUser.getUsername(),
                            appUser.getPassword(),
                            List.of(new SimpleGrantedAuthority(appUser.getRole().name()))
                    )
            );

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .build();

            return new DefaultResponse("200", "Success", "User Registered Successfully", authResponse);
        } catch (Exception e) {
             log.error("Error is getting while saving user {}", e.getMessage());
             return new DefaultResponse("400", "Failed", "User already existed from the given email");
        }
    }

    public DefaultResponse login(LoginRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            AppUser user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            String token = jwtService.generateToken(
                    new org.springframework.security.core.userdetails.User(
                            user.getUsername(),
                            user.getPassword(),
                            List.of(new SimpleGrantedAuthority(user.getRole().name()))
                    )
            );

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .build();

            log.info("User logged in successfully");
            return new DefaultResponse("200", "Success", "User logged in successfully",authResponse);

        } catch (Exception e) {
            log.error("Error is getting while logging in {}", e.getMessage());
            return new DefaultResponse("400", "Failed", "Error is getting while logging in");
        }
    }

    public DefaultResponse getProfileDetails(UserDetails userDetails){
        try{
            Optional<AppUser> user = userRepository.findByUsername(userDetails.getUsername());
            if(user.isEmpty()){
                throw new RuntimeException("user not found");
            }
            AppUser appUser = user.get();
            ProfileDetailsResponse response = ProfileDetailsResponse.builder()
                    .username(userDetails.getUsername())
                    .email(appUser.getEmail())
                    .role(appUser.getRole().toString())
                    .build();

            return new DefaultResponse("200", "Success", "profile Details fetched successfully", response);
        } catch (Exception e) {
           log.info("profile Details fetched failed");
            return new DefaultResponse("400", "Failed", "Error is fetching profile details");
        }
    }

    public DefaultResponse getDashboardDetails(UserDetails userDetails){
        try{
            List<LoanData> loanApplication = loanDataRepository.findByCreatedBy(userDetails.getUsername());
            int noOfApplications = loanApplication.size();

            List<LoanData> acceptedList = loanDataRepository.findByCreatedByAndStatus(userDetails.getUsername(), LoanStatus.APPROVED.name());

            Double totalAmount = acceptedList.stream()
                    .mapToDouble(loan -> loan.getLoanAmount() != null ? loan.getLoanAmount() : 0.0)
                    .sum();

            List<LoanData> rejectedList = loanDataRepository.findByCreatedByAndStatus(userDetails.getUsername(), LoanStatus.REJECTED.name());

            List<LoanData> recentApplications = loanDataRepository.findByCreatedByAndCreatedAt(userDetails.getUsername(), LocalDate.now());
            if(recentApplications.isEmpty()){
                return new DefaultResponse("200", "Success", "No recent applications");
            }

            List<RecentApplicationResponse> responseList = recentApplications.stream()
                    .map(loan -> new RecentApplicationResponse(
                            loan.getId(),
                            loan.getStatus(),
                            loan.getLoanAmount(),
                            loan.getCreatedAt()
                    ))
                    .toList();


            int acceptedCount = acceptedList.size();
            int rejectedCount = rejectedList.size();

            DashboardDetailsResponse response = DashboardDetailsResponse.builder()
                    .noOfApplications(noOfApplications)
                    .approvedApplications(acceptedCount)
                    .rejectedApplications(rejectedCount)
                    .totalLoanAmount(totalAmount)
                    .recentApplications(responseList)
                    .build();

            return new DefaultResponse("200", "Success", "profile Details fetched successfully", response);

        } catch (Exception e) {
            log.info("getting error while fetching dashboard details");
            return new DefaultResponse("400", "Failed", "getting error while fetching dashboard details");
        }

    }

}
