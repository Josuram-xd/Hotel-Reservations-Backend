package com.hotel.reservations.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Error payload: same shape for every non-2xx response. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(int status, String code, String message, List<FieldErrorResponse> details) {

    /** One field-level validation error inside the error details. */
    public record FieldErrorResponse(String field, String message) {
    }
}
