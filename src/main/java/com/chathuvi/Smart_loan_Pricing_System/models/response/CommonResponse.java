package com.chathuvi.Smart_loan_Pricing_System.models.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse {
    private String code;
    private String title;
    private String message;
    private Object data = new Object();

    public CommonResponse(String code, String title, String message) {
        //String lang = MDC.get(AppConstants.MDC_LANGUAGE);
        this.code = code;
        this.title = title/* AppLanguageService.getLangStr(lang, title)*/;
        this.message = message /*AppLanguageService.getLangStr(lang, message)*/;
        this.data = new Object();
    }
}
