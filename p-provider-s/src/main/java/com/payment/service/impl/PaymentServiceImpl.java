package com.payment.service.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.payment.http.HttpRequest;
import com.payment.http.HttpServiceEngine;
import com.payment.pojo.CreateOrderReq;
import com.payment.pojo.OrderResponse;
import com.payment.service.interfaces.PaymentService;
import com.payment.services.PaymentValidator;
import com.payment.services.TokenService;
import com.payment.services.helper.CaptureOrderHelper;
import com.payment.services.helper.CreateOrderHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

   
private final TokenService tokenService;
	
	private final HttpServiceEngine httpServiceEngine;
	
	@Value("${paypal.createOrder.url}")
	private String createOrderUrl;
	
	private final CreateOrderHelper	createOrderHelper;
	
	private final CaptureOrderHelper captureOrderHelper;
	
	private final PaymentValidator paymentValidator;
	
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {
		log.info("Creating order in PaymentServiceImpl|| createOrderReq:{}",
				createOrderReq);
		
		paymentValidator.validateCreateOrder(createOrderReq);
		
		log.info("Create order request validated successfully");
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access token retrieved: {}", accessToken);
		
		HttpRequest httpRequest = createOrderHelper.prepareCreateOrderHttpRequest(
				createOrderReq, accessToken);
		log.info("Prepared HttpRequest for OAuth call: {}", httpRequest);
		
		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpRequest(httpRequest);
		log.info("HTTP response from HttpServiceEngine: {}", httpResponse);

		OrderResponse orderResponse = createOrderHelper.handlePaypalResponse(httpResponse);
		log.info("Final OrderResponse to be returned: {}", orderResponse);
		
		return orderResponse;
	}

	public OrderResponse captureOrder(String orderId) {
		log.info("Capturing order in PaymentServiceImpl|| orderId:{}",
				orderId);
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access token retrieved: {}", accessToken);
		
		HttpRequest httpRequest = captureOrderHelper.prepareCaptureOrderHttpRequest(
				orderId, accessToken);
		log.info("Prepared HttpRequest for capturing order httpRequest: {}", httpRequest);
		
		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpRequest(httpRequest);
		log.info("HTTP response from HttpServiceEngine: {}", httpResponse);
		
		OrderResponse orderResponse = captureOrderHelper.handlePaypalResponse(httpResponse);
		log.info("Final OrderResponse to be returned: {}", orderResponse);
		
		return orderResponse;
	}

	
  
}
