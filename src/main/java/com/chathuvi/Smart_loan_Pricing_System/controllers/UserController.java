package com.chathuvi.Smart_loan_Pricing_System.controllers;

import com.chathuvi.Smart_loan_Pricing_System.models.request.LoginRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.RegisterRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.DefaultResponse;
import com.chathuvi.Smart_loan_Pricing_System.services.ReportService;
import com.chathuvi.Smart_loan_Pricing_System.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ReportService reportService;

    @PostMapping("/register")
    public DefaultResponse sendLoanDetails(@RequestBody RegisterRequest request) {
        DefaultResponse response = userService.saveData(request);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(), response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

    @PostMapping("/login")
    public DefaultResponse userLogin(@RequestBody LoginRequest request) {
        DefaultResponse response = userService.login(request);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(), response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

//    @GetMapping("/profile")
//    public String getProfile(Authentication authentication) {
//
//        String username = authentication.getName();
//
//        return "Current user: " + username;
//    }

    @GetMapping("/profile")
    public DefaultResponse getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        DefaultResponse response = userService.getProfileDetails(userDetails);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(), response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

//    @PostMapping("/update-profile")
//    public DefaultResponse updateUserProfile(@AuthenticationPrincipal UserDetails userDetails, ProfileDetailsRequest detailsRequest) {
//        DefaultResponse response = userService.login(userDetails, detailsRequest);
//        if (response.getCode().equals("200")) {
//            return new DefaultResponse("200", "Success", response.getMessage(), response.getData());
//        } else if (response.getCode().equals("400")) {
//            return new DefaultResponse("400", "Failed", response.getMessage());
//        } else {
//            return new DefaultResponse("500", "Internal server error", response.getMessage());
//        }
//    }

    @GetMapping("/dashboard")
    public DefaultResponse getLoanApplication(@AuthenticationPrincipal UserDetails userDetails){
        DefaultResponse response = userService.getDashboardDetails(userDetails);
        if (response.getCode().equals("200")) {
            return new DefaultResponse("200", "Success", response.getMessage(),response.getData());
        } else if (response.getCode().equals("400")) {
            return new DefaultResponse("400", "Failed", response.getMessage());
        } else {
            return new DefaultResponse("500", "Internal server error", response.getMessage());
        }
    }

    @GetMapping("/dashboard/export")
    public ResponseEntity<byte[]> exportDashboardReport() {
        byte[] excelFile = reportService.exportDashboardReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }


}
