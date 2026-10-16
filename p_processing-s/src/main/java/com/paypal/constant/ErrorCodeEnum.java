package com.paypal.constant;



import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

	GENERIC_ERROR("20000", "Something went wrong. Please try again later."),
	RESOURCE_NOT_FOUND("20001", "Invalid URL. Please check and try again."), 
	PAYPAL_PROVIDER_SERVICE_UNAVAILABLE("20002", "paypal-provider service is currently unavailable. Please try again later."),
	NO_STATUS_PROCESSOR_FOUND("20003", "No status processor found"), 
	PAYPAL_PROVIDER_UNKNOWN_ERROR("20004", "Unknown error occurred in paypal-provider service."), 
	ERROR_UPDATING_TRANSACTION("20005", "Error updating transaction details.");

    private final String errorCode;
    private final String errorMessage;

    ErrorCodeEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
