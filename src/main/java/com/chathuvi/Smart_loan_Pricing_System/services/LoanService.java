package com.chathuvi.Smart_loan_Pricing_System.services;

import com.chathuvi.Smart_loan_Pricing_System.entity.LoanData;
import com.chathuvi.Smart_loan_Pricing_System.enums.LoanStatus;
import com.chathuvi.Smart_loan_Pricing_System.models.request.FeedbackRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.LoanDetailRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.*;
import com.chathuvi.Smart_loan_Pricing_System.repository.LoanDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanService {

    @Value("${model.service.url}")
    private String modelUrl;

    @Value("${bandit.service.url}")
    private String banditUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final LoanDataRepository loanDataRepository;
    private final ObjectMapper objectMapper;

    public DefaultResponse sendLoanDataToModel(LoanDetailRequest request, UserDetails userDetails) {

        Map<Double, Double> acceptanceMap = new HashMap<>();

        List<LoanDataResponse> loanDataResponseList = new ArrayList<>();

        LoanData loanData = saveLoanData(request, userDetails);

        for (Double rate : request.getInterestRates()) {

            LoanDataResponse loanDataResponse = new LoanDataResponse();

            Double customizeRate = rate/100;

            Map<String, Object> payload = buildModelPayload(request, customizeRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = callModelWithRetry(entity);

            Double acceptanceProb =
                    Double.valueOf(response.getBody().get("acceptance_probability").toString());

            log.info("interest rate {} acceptance prob {}", rate , acceptanceProb);

//            acceptanceMap.put(rate, acceptanceProb);

            // Step 2 → Expected Profit
            double expectedProfit = calculateExpectedProfit(request.getLoanAmount(),customizeRate, acceptanceProb);
            loanDataResponse.setRate(String.valueOf(rate));
            BigDecimal percentage = BigDecimal.valueOf(acceptanceProb * 100)
                    .setScale(2, RoundingMode.DOWN);
            loanDataResponse.setProbabilityRate(percentage.toString());
            BigDecimal formattedProfit = BigDecimal.valueOf(expectedProfit)
                    .setScale(2, RoundingMode.DOWN);

            loanDataResponse.setProfit(formattedProfit.toString());
            loanDataResponseList.add(loanDataResponse);

            acceptanceMap.put(rate, expectedProfit);
        }

//        log.info("acceptance probabilities {}", acceptanceMap);
//        loanData.setAcceptanceProbs(acceptanceMap);

        // Step 2 → Expected Profit
//        Map<Double, Double> expectedProfitMap = calculateExpectedProfit(request.getLoanAmount(), acceptanceMap);
//        loanData.setExpectedProfits(expectedProfitMap);
        log.info("Expected profits {}", acceptanceMap);

        // Step 3 → Send to Bandit
        Map<String, Object> banditResponse = callBandit(request, acceptanceMap);

        log.info("bandit response {}", banditResponse);

        //check rate with above loop
        for(LoanDataResponse objList : loanDataResponseList){
            Double rate = Double.valueOf(objList.getRate());
            if(rate.equals(Double.valueOf(banditResponse.get("selected_rate").toString()))){
                log.info("########## {}", objList);
                objList.setStatus("Recommended");
                loanData.setAcceptanceProb(Double.valueOf(objList.getProbabilityRate()));
                loanData.setExpectedProfit(Double.valueOf(objList.getProfit()));
            }
        }

        loanData.setContextId(banditResponse.get("context_id").toString());
        loanData.setSelectedRate(Double.valueOf(banditResponse.get("selected_rate").toString()));

        loanDataRepository.save(loanData);

        return new DefaultResponse(
                "200",
                "SUCCESS",
                "Rate selected",
                loanDataResponseList
        );
    }

    private ResponseEntity<Map> callModelWithRetry(HttpEntity<Map<String, Object>> entity) {
        int maxAttempts = 3;
        long delayMs = 2000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Calling model service attempt {}/{}", attempt, maxAttempts);

                return restTemplate.postForEntity(
                        modelUrl + "/predict",
                        entity,
                        Map.class
                );

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn("Model service returned 429 on attempt {}/{}", attempt, maxAttempts);

                if (attempt == maxAttempts) {
                    throw e;
                }

                sleepBeforeRetry(delayMs);

            } catch (Exception e) {
                log.error("Model service call failed on attempt {}/{}: {}", attempt, maxAttempts, e.getMessage());

                if (attempt == maxAttempts) {
                    throw e;
                }

                sleepBeforeRetry(delayMs);
            }
        }

        throw new RuntimeException("Model service call failed after retries");
    }

    private void sleepBeforeRetry(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry sleep interrupted", e);
        }
    }

    private Map<String, Object> buildModelPayload(LoanDetailRequest request, Double rate) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("LoanAmount", request.getLoanAmount());
        payload.put("LoanDuration", request.getLoanDuration());
        payload.put("DebtToIncomeRatio", request.getTotalDebtToIncomeRatio());
        payload.put("CreditScore", request.getCreditScore());
        payload.put("NumberOfOpenCreditLines", request.getNumberOfOpenCreditLines());
        payload.put("AnnualIncome", request.getAnnualIncome());
        payload.put("SavingsAccountBalance", request.getSavingsAccountBalance());
        payload.put("TotalLiabilities", request.getTotalLiabilities());
        payload.put("Age", request.getAge());
        payload.put("EducationLevel", request.getEducationLevel());
        payload.put("MaritalStatus", request.getMaritalStatus());
        payload.put("EmploymentStatus", request.getEmploymentStatus());
        payload.put("InterestRate", rate);
        payload.put("PaymentHistory", request.getPaymentHistory());

        return payload;
    }

    private double calculateExpectedProfit(Double loanAmount, double rate, double acceptance) {

        double revenue = loanAmount * (rate / 100);  // simplified
        return acceptance * revenue;

    }

    private Map<String, Object> callBandit(
            LoanDetailRequest request,
            Map<Double, Double> expectedProfitMap
    ) {

        Map<String, Object> payload =
                buildBanditSelectPayload(request, expectedProfitMap);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        banditUrl + "/bandit/select",
                        payload,
                        Map.class
                );

        return response.getBody();  // contains selected_rate + context_id
    }


    private Map<String, Object> buildBanditSelectPayload(
            LoanDetailRequest request,
            Map<Double, Double> expectedProfitMap
    ) {
        Map<String, Object> payload = new HashMap<>();

        // 🔹 MUST MATCH BanditSelectRequest
        payload.put("features", Map.of(
                "AnnualIncome", request.getAnnualIncome(),
                "CreditScore", request.getCreditScore()
        ));

        payload.put("expected_profits", expectedProfitMap);

        return payload;
    }


    public DefaultResponse sendFeedback(FeedbackRequest request, UserDetails userDetails) {

       LoanData loanDetails = loanDataRepository.findById(request.getApplicationId()).orElse(null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("context_id", loanDetails.getContextId());
        payload.put("rate", loanDetails.getSelectedRate());
        if(request.getIsAccepted()){
            loanDetails.setStatus(LoanStatus.APPROVED.name());
            payload.put("reward", loanDetails.getExpectedProfit());
        } else {
            loanDetails.setStatus(LoanStatus.REJECTED.name());
            payload.put("reward", 0.0);
        }
        if (StringUtils.hasText(request.getReason())) {
            loanDetails.setReason(request.getReason());
        }
        loanDetails.setUpdatedAt(LocalDate.now());
        loanDetails.setUpdatedBy(userDetails.getUsername());
        loanDataRepository.save(loanDetails);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        banditUrl + "/bandit/update",
                        payload,
                        Map.class
                );

        log.info("Bandit response: {}", response.getBody());
        log.info("Bandit updated successfully");
        return new DefaultResponse(
                "200",
                "Success",
                "Bandit updated successfully"
        );
    }

    public LoanData saveLoanData(LoanDetailRequest request, UserDetails userDetails) {
        LoanData loanData = LoanData.builder()
                .customerName(request.getName())
                .age(request.getAge())
                .loanDuration(request.getLoanDuration())
                .creditScore(request.getCreditScore())
                .educationLevel(request.getEducationLevel())
                .annualIncome(request.getAnnualIncome())
                .employmentStatus(request.getEmploymentStatus())
                .paymentHistory(request.getPaymentHistory())
                .savingsAccountBalance(request.getSavingsAccountBalance())
                .totalLiabilities(request.getTotalLiabilities())
                .numberOfOpenCreditLines(request.getNumberOfOpenCreditLines())
                .loanAmount(request.getLoanAmount())
                .totalDebtToIncomeRatio(request.getTotalDebtToIncomeRatio())
                .maritalStatus(request.getMaritalStatus())
                .createdAt(LocalDate.now())
                .createdBy(userDetails.getUsername())
                .status(LoanStatus.PENDING.name())
                .build();

        loanDataRepository.save(loanData);
        return loanData;
    }

    public DefaultResponse getListOfApplications(UserDetails userDetails){

        List<LoanData> loanApplication = loanDataRepository.findByCreatedBy(userDetails.getUsername());
        int noOfApplications = loanApplication.size();
        List<LoanListResponse> loanListResponse = new ArrayList<>();

        for (LoanData loan : loanApplication) {

            LoanListResponse response = LoanListResponse.builder()
                    .loanId(String.valueOf(loan.getId()))
                    .customerName(loan.getCustomerName())
                    .createdAt(loan.getCreatedAt() != null ? loan.getCreatedAt().toString() : null)
                    .offeredRate(loan.getSelectedRate() != null ? String.valueOf(loan.getSelectedRate()) : null)
                    .confidence(loan.getAcceptanceProb() != null ? String.valueOf(loan.getAcceptanceProb()) : null)
                    .status(loan.getStatus())
                    .build();

            loanListResponse.add(response);
        }

        List<LoanData> acceptedList =
                loanDataRepository.findByCreatedByAndStatus(userDetails.getUsername(), LoanStatus.APPROVED.name());

        List<LoanData> rejectedList =
                loanDataRepository.findByCreatedByAndStatus(userDetails.getUsername(), LoanStatus.REJECTED.name());

        int acceptedCount = acceptedList.size();
        int rejectedCount = rejectedList.size();
        int total = acceptedCount + rejectedCount;

        double acceptanceRate = total > 0 ? (acceptedCount * 100.0) / total : 0;
        BigDecimal acceptanceRateRound = BigDecimal.valueOf(acceptanceRate)
                .setScale(2, RoundingMode.DOWN);
        double rejectionRate = total > 0 ? (rejectedCount * 100.0) / total : 0;
        BigDecimal rejectRateRound = BigDecimal.valueOf(rejectionRate)
                .setScale(2, RoundingMode.DOWN);

        LoanHistoryResponse loanHistoryResponse = new LoanHistoryResponse();
        loanHistoryResponse.setLoanListResponse(loanListResponse);
        loanHistoryResponse.setNoOfApplications(String.valueOf(noOfApplications));
        loanHistoryResponse.setAvgAcceptance(String.valueOf(acceptanceRateRound));
        loanHistoryResponse.setAvgRejection(String.valueOf(rejectRateRound));

        return new DefaultResponse("200", "Success", "Loan history fetched successfully", loanHistoryResponse);
    }

    public DefaultResponse getLoanSpecificDetails(Long applicationId) {
        LoanData loanData = loanDataRepository.findById(applicationId).orElse(null);

        if (loanData == null) {
            return new DefaultResponse("400", "Failed", "Loan not found");
        }

        double P = loanData.getLoanAmount();          // Loan amount
        double annualRate = loanData.getSelectedRate(); // e.g. 11
        int n = loanData.getLoanDuration().intValue(); // months

        // Monthly interest rate
        double r = annualRate / 12 / 100;

        // EMI calculation
        double emi;
        if (r == 0) {
            emi = P / n; // no interest case
        } else {
            emi = (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        }

        LoanSpecificResponse loanSpecificResponse = new LoanSpecificResponse();
        loanSpecificResponse.setApplicationId(loanData.getId().toString());
        loanSpecificResponse.setLoanAmount(P + "");
        loanSpecificResponse.setTerm(n + "");
        loanSpecificResponse.setCustomerName(loanData.getCustomerName());
        loanSpecificResponse.setInterestRate(annualRate + "");
        loanSpecificResponse.setMonthlyPayment(String.format("%.2f", emi));

        log.info("Loan details fetched successfully {}", objectMapper.writeValueAsString(loanSpecificResponse));

        return new DefaultResponse("200", "Success", "Loan details fetched successfully", loanSpecificResponse);
    }


}
