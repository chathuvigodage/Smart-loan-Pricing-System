package com.chathuvi.Smart_loan_Pricing_System.services;

import com.chathuvi.Smart_loan_Pricing_System.entity.LoanData;
import com.chathuvi.Smart_loan_Pricing_System.models.request.FeedbackRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.request.LoanDetailRequest;
import com.chathuvi.Smart_loan_Pricing_System.models.response.DefaultResponse;
import com.chathuvi.Smart_loan_Pricing_System.repository.LoanDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String MODEL_URL = "http://127.0.0.1:8000/predict";
    private final LoanDataRepository loanDataRepository;

    public DefaultResponse sendLoanDataToModel(LoanDetailRequest request) {

        Map<Double, Double> acceptanceMap = new HashMap<>();

        LoanData loanData = saveLoanData(request);

        for (Double rate : request.getInterestRates()) {

            Map<String, Object> payload = buildModelPayload(request, rate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(MODEL_URL, entity, Map.class);

            Double acceptanceProb =
                    Double.valueOf(response.getBody().get("acceptance_probability").toString());

            acceptanceMap.put(rate, acceptanceProb);

        }

        log.info("acceptance probabilities {}", acceptanceMap);
        loanData.setAcceptanceProbs(acceptanceMap);

        // Step 2 → Expected Profit
        Map<Double, Double> expectedProfitMap = calculateExpectedProfit(request.getLoanAmount(), acceptanceMap);
        loanData.setExpectedProfits(expectedProfitMap);
        log.info("Expected profits {}", expectedProfitMap);

        // Step 3 → Send to Bandit
        Map<String, Object> banditResponse = callBandit(request, expectedProfitMap);

        loanData.setContextId(banditResponse.get("context_id").toString());
        loanData.setSelectedRate(Double.valueOf(banditResponse.get("selected_rate").toString()));

        loanDataRepository.save(loanData);

        return new DefaultResponse(
                "200",
                "SUCCESS",
                "Rate selected",
                banditResponse
        );
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

    private Map<Double, Double> calculateExpectedProfit(Double loanAmount, Map<Double, Double> acceptanceMap) {

        Map<Double, Double> profitMap = new HashMap<>();

        for (Map.Entry<Double, Double> entry : acceptanceMap.entrySet()) {

            Double rate = entry.getKey();
            Double acceptance = entry.getValue();

            Double revenue = loanAmount * (rate / 100);  // simplified
            Double expectedProfit = acceptance * revenue;

            profitMap.put(rate, expectedProfit);
        }

        return profitMap;
    }

    private Map<String, Object> callBandit(
            LoanDetailRequest request,
            Map<Double, Double> expectedProfitMap
    ) {

        Map<String, Object> payload =
                buildBanditSelectPayload(request, expectedProfitMap);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "http://127.0.0.1:9000/bandit/select",
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


    public DefaultResponse sendFeedback(FeedbackRequest request){

        Map<String, Object> payload = new HashMap<>();
        payload.put("context_id", request.getContextId());
        payload.put("rate", request.getRate());
        payload.put("reward", request.getProfit());

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "http://127.0.0.1:9000/bandit/update",
                        payload,
                        Map.class
                );

        log.info("Bandit response: {}", response.getBody());

        return new DefaultResponse(
                "200",
                "Success",
                "Bandit updated successfully"
        );
    }

    public LoanData saveLoanData(LoanDetailRequest request){
        LoanData loanData = LoanData.builder()
                .age(request.getAge())
                .loanDuration(request.getLoanDuration())
                .creditScore(request.getCreditScore())
                .educationLevel(request.getEducationLevel())
                .annualIncome(request.getAnnualIncome())
                .employmentStatus(request.getEmploymentStatus())
                .interestRates(request.getInterestRates())
                .paymentHistory(request.getPaymentHistory())
                .savingsAccountBalance(request.getSavingsAccountBalance())
                .totalLiabilities(request.getTotalLiabilities())
                .numberOfOpenCreditLines(request.getNumberOfOpenCreditLines())
                .loanAmount(request.getLoanAmount())
                .totalDebtToIncomeRatio(request.getTotalDebtToIncomeRatio())
                .maritalStatus(request.getMaritalStatus())
                .build();

        loanDataRepository.save(loanData);
        return loanData;
    }



}
