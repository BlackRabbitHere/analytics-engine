package com.analytics.analytics_engine.exception;

// for validating same category name should not occur
public class APIException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public APIException(ErrorCode errorCode){
        this(errorCode,null);
    }

    public APIException(ErrorCode errorCode,Throwable cause) {
        super(errorCode.getMessage(),cause);
        this.errorCode=errorCode;
    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }

}
