package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.*;
import org.slf4j.MDC;

import java.util.HashMap;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultResponse {
    private String code;
    private String title;
    private String message;
    private Object data;

    public DefaultResponse(String code, String title, String message) {
        this.code = code;
        this.title = title;
        this.message = message;
    }
}
