package com.paymentl.Constant;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

		
	GENERIC_ERROR("10000" , "Something went Wrong  please try again later  "),
	 CURRENT_CODE_REQUIRED("30001" , "Currency code is required field and cannot to null / blank "),
	 RETURN_URL_REQUIRED("30002" , "Return Url is required field and cannot be null / blank "),
	 INVALID_REQUEST("30003" , "Invalid request payload"),
	 INVALID_AMOUNT("30004" , "Amount must be greater than zero "),
	 CANCEL_URL_REQUIRED("30005" , "Cancel Url is required field and cannot be null / blank "),
	 PAYPAL_SERVICE_UNAVAILABLE("40001" , "PayPal service is currently unavailable , please try again later "),
	 PAYPAL_API_ERROR("40002" , "PayPal API returned an error response, please try again later "),
	 PAYPAL_API_TIMEOUT("40003" , "PayPal API request timed out, please try again later "),
	 PAYPAL_API_UNAUTHORIZED("40004" , "Unauthorized access to PayPal API, please check your credentials "),
	 PAYPAL_API_FORBIDDEN("40005" , "Forbidden access to PayPal API, please check your permissions "),
	 PAYPAL_API_NOT_FOUND("40006" , "PayPal API endpoint not found, please check the URL "),
	 PAYPAL_API_INTERNAL_ERROR("50001" , "Internal server error occurred while processing the request "),
	 PAYPAL_ERROR("300007" , "<paypal error>"), PAYPAL_UNKNOWN_ERROR("30002","error"), CURRENCY_CODE_REQUIRED("30008","Currency code is required field and cannot be null / blank");
	
	private final String errorCode;
	private final String errorMessage;
	
	ErrorCodeEnum(String errorCode , String errorMessage){
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
}
