package com.hotel.reservations.web;

import com.hotel.reservations.dto.ApiErrorResponse;
import com.hotel.reservations.dto.ApiErrorResponse.FieldErrorResponse;
import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.exception.InvalidServiceException;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.exception.RoomNotAvailableException;
import com.hotel.reservations.exception.RoomNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Translates exceptions into the uniform error payload of the API contract:
 * {@code { status, code, message, details? }} with stable codes the frontend
 * maps to Spanish messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        List<FieldErrorResponse> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Some fields are invalid", details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = "Invalid value '%s'".formatted(exception.getValue());
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Some fields are invalid",
                List.of(new FieldErrorResponse(exception.getName(), message)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleUnreadableBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "The request body is missing or malformed", null);
    }

    @ExceptionHandler(InvalidDatesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidDates(InvalidDatesException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_DATES", exception.getMessage(), null);
    }

    @ExceptionHandler(InvalidServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidService(InvalidServiceException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_SERVICE", exception.getMessage(), null);
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleRoomNotAvailable(RoomNotAvailableException exception) {
        return error(HttpStatus.CONFLICT, "ROOM_NOT_AVAILABLE", exception.getMessage(), null);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleRoomNotFound(RoomNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", exception.getMessage(), null);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleReservationNotFound(ReservationNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", exception.getMessage(), null);
    }

    @ExceptionHandler(InvalidStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleInvalidState(InvalidStateException exception) {
        return error(HttpStatus.CONFLICT, "INVALID_STATE", exception.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleUnexpected(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", null);
    }

    private static ApiErrorResponse error(HttpStatus status, String code, String message,
                                          List<FieldErrorResponse> details) {
        return new ApiErrorResponse(status.value(), code, message, details);
    }
}
