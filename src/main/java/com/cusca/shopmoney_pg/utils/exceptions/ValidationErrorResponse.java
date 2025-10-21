package com.cusca.shopmoney_pg.utils.exceptions;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> errors;
}
