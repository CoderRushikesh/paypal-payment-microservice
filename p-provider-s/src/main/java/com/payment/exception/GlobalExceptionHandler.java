package com.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.payment.pojo.ErrorResponse;
import com.paymentl.Constant.ErrorCodeEnum;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
			
   
	
    @ExceptionHandler(PaypalProviderException.class)
	public ResponseEntity<ErrorResponse> handlePaypalProviderException(PaypalProviderException ex) {
	log.error("PaypalProviderException: ", ex);
    	ErrorResponse error = new ErrorResponse(ex.getErrorCode() , ex.getMessage());	
		return new ResponseEntity<>(error,  ex.getHttpStatus());
	}
    
    
    @ExceptionHandler(Exception.class)
	 public ResponseEntity<ErrorResponse> handleException(Exception ex)
	 // create method type of ErrorRespose the name of the method is HANDLE_paypal_Exception , and passed 
	 //pyapalProviderException object 
	 {
		 log.error("handling paypalProviderException : {} " , ex.getMessage() , ex);
		 ErrorResponse error = new ErrorResponse(ErrorCodeEnum.GENERIC_ERROR.getErrorCode(), ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());
		 // createing the create the object of error response by passing errorCode , errorMessage 
		 
		 return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
		 // it's provided by spring boot to return the error and httpStatus 
		 
		 
	 }
	
}
