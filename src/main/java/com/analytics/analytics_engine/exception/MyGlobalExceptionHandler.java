package com.analytics.analytics_engine.exception;

import com.analytics.analytics_engine.dto.ApiResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<Object> handleAPIException(APIException ex) {
        ErrorCode error = ex.getErrorCode();

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(errorBody(error, Map.of()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : "_global";

            String message = error.getDefaultMessage() != null
                    ? error.getDefaultMessage()
                    : "Invalid value";

            fieldErrors
                    .computeIfAbsent(field, key -> new ArrayList<>())
                    .add(message);
        });

        ErrorCode error = ErrorCode.VALIDATION_FAILED;

        return handleExceptionInternal(
                ex,
                errorBody(error, fieldErrors),
                headers,
                error.getHttpStatus(),
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        if (statusCode.is5xxServerError()) {
            logger.error("Server error while processing request", ex);
        }

        Object responseBody = body;

        if (!(body instanceof ApiResponseDTO)) {
            if (ex instanceof HttpMessageNotReadableException) {
                responseBody = errorBody(
                        ErrorCode.INVALID_REQUEST_BODY,
                        Map.of()
                );
            } else if (statusCode.is5xxServerError()) {
                responseBody = new ApiResponseDTO(
                        false,
                        statusCode.value(),
                        ErrorCode.INTERNAL_ERROR.name(),
                        ErrorCode.INTERNAL_ERROR.getMessage(),
                        Map.of()
                );
            } else {
                HttpStatus status = HttpStatus.resolve(statusCode.value());

                responseBody = new ApiResponseDTO(
                        false,
                        statusCode.value(),
                        "HTTP_" + statusCode.value(),
                        status != null
                                ? status.getReasonPhrase()
                                : "Request could not be processed",
                        Map.of()
                );
            }
        }

        return super.handleExceptionInternal(
                ex,
                responseBody,
                headers,
                statusCode,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex) {
        logger.error("Unexpected error while processing request", ex);

        ErrorCode error = ErrorCode.INTERNAL_ERROR;

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(errorBody(error, Map.of()));
    }

    private ApiResponseDTO errorBody(
            ErrorCode error,
            Map<String, List<String>> fieldErrors
    ) {
        return new ApiResponseDTO(
                false,
                error.getHttpStatus().value(),
                error.name(),
                error.getMessage(),
                fieldErrors
        );
    }
}