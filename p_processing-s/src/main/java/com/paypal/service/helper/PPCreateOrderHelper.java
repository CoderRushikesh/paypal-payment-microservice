package com.paypal.service.helper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.paypal.constant.ErrorCodeEnum;
import com.paypal.dto.TransactionDto;
import com.paypal.exception.ProcessingServiceException;
import com.paypal.http.HttpRequest;
import com.paypal.paypalprovider.PPCreateOrderReq;
import com.paypal.paypalprovider.PPErrorResponse;
import com.paypal.paypalprovider.PPOrderResponse;
import com.paypal.pojo.InitiatePaymentRequest;
import com.paypal.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PPCreateOrderHelper {

	private final   JsonUtil jsonUtil;
	
	public  HttpRequest prepareHttpRequest(String tnxReference, InitiatePaymentRequest initiatePaymentRequest, TransactionDto txnDto) {
		
	log.info("Preparing HTTP request for PayPal order creation with transaction reference: {} || initiatePaymentRequest {} || txnDto {}", tnxReference, initiatePaymentRequest, txnDto);
		
		HttpHeaders headers = new HttpHeaders();
		// headers.setBearerAuth(accessToken); there is no requirement within the same system 
		
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		PPCreateOrderReq ppCreateOrderRequest = new PPCreateOrderReq();
		ppCreateOrderRequest.setAmount(txnDto.getAmount().doubleValue());
		ppCreateOrderRequest.setCurrencyCode(txnDto.getCurrency());
		ppCreateOrderRequest.setReturnUrl(initiatePaymentRequest.getSuccessUrl());
		ppCreateOrderRequest.setCancelUrl(initiatePaymentRequest.getCancelUrl());

		
		 String requestAsJson = jsonUtil.toJson(ppCreateOrderRequest);
		
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl("http://localhost:8083/Payments");
		
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(requestAsJson);
		
		
		
		
		return httpRequest;
		
		
	}

	public PPOrderResponse processResponse(ResponseEntity<String> httpResponse) {
	
		log.info("Processing HTTP response from PayPal order creation with status code: {} and body: {}", httpResponse.getStatusCode(), httpResponse.getBody());
	
		if(httpResponse.getStatusCode().equals(HttpStatus.OK)) {
			log.info("PayPal order created successfully with response: {}",
					httpResponse.getBody());}
			
	PPOrderResponse responseObj	=	jsonUtil.fromJson(httpResponse.getBody(), PPOrderResponse.class);
			// Further processing can be done here, such as updating transaction status in DB
	   
	if(responseObj != null && responseObj.getPaypalStatus()
			!= null && responseObj.getOrderId() != null
			&& responseObj.getRedirectUrl() != null) {
	 
	   log.info("Extracted OrderId: {}, PaypalStatus: {}, RedirectUrl: {} from PayPal response {} ", responseObj);
		} 
	else {
			log.error("Failed to extract valid response from PayPal order creation. Response body: {}", httpResponse.getBody());
		}
	
	
	
	if(httpResponse.getStatusCode().is4xxClientError() || httpResponse.getStatusCode().is5xxServerError()) {
		log.error("Error response received from PayPal order creation. Status code: {}, Response body: {}", httpResponse.getStatusCode(), httpResponse.getBody());
		
PPErrorResponse errorResponse =	jsonUtil.fromJson(httpResponse.getBody(), PPErrorResponse.class);
		
	throw new ProcessingServiceException(
              errorResponse.getErrorCode(),
			  errorResponse.getErrorMessage(),
	          HttpStatus.valueOf(httpResponse.getStatusCode().value()));
	}
	
	
	
	log.error("Unexpected response received from PayPal order creation. Status code: {}, Response body: {}", httpResponse.getStatusCode(), httpResponse.getBody());
	
	
	
			  
	
	
	return responseObj;
		
		
	}
			
	
	
}
