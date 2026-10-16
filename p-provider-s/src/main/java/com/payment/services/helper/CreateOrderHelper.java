package com.payment.services.helper;

import java.util.Collections;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.payment.exception.PaypalProviderException;
import com.payment.http.HttpRequest;
import com.payment.pojo.CreateOrderReq;
import com.payment.pojo.OrderResponse;
import com.payment.req.Amount;
import com.payment.req.ExperienceContext;
import com.payment.req.OrderRequest;
import com.payment.req.PaymentSource;
import com.payment.req.Paypal;
import com.payment.req.PurchaseUnit;
import com.payment.res.PaypalLink;
import com.payment.res.PaypalOrder;
import com.payment.res.error.PaypalErrorResponse;
import com.payment.util.JsonUtil;
import com.payment.util.PaypalOrderUtil;
import com.paymentl.Constant.Constant;
import com.paymentl.Constant.ErrorCodeEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderHelper {

	private final JsonUtil jsonUtil;

	@Value("${paypal.createOrder.url}")
	private String createOrderUrl;

	public HttpRequest prepareCreateOrderHttpRequest(
			CreateOrderReq createOrderReq, String accessToken) {
		HttpHeaders headers = prepareHeader(accessToken);

		String requestAsJson = prepareReqBodyAsJson(createOrderReq);

		// create HttpRequest
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);

		httpRequest.setUrl(createOrderUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(requestAsJson);
		return httpRequest;
	}

	private String prepareReqBodyAsJson(CreateOrderReq createOrderReq) {
		// Create amount object
		Amount amount = new Amount();
		amount.setCurrencyCode(createOrderReq.getCurrencyCode());

		// read the amount from createOrderReq and convert to 2 decimal places format string
		String amtStr = String.format(Constant._2F, createOrderReq.getAmount());
		amount.setValue(amtStr);

		// Create purchase unit
		PurchaseUnit unit = new PurchaseUnit();
		unit.setAmount(amount);

		// Experience context
		ExperienceContext ctx = new ExperienceContext();
		ctx.setPaymentMethodPreference(Constant.IMMEDIATE_PAYMENT_REQUIRED);
		ctx.setLandingPage(Constant.LANDING_PAGE_LOGIN);
		ctx.setShippingPreference(Constant.NO_SHIPPING);
		ctx.setUserAction(Constant.USER_ACTION_PAY_NOW);
		ctx.setReturnUrl(createOrderReq.getReturnUrl());
		ctx.setCancelUrl(createOrderReq.getCancelUrl());

		// Paypal object
		Paypal paypal = new Paypal();
		paypal.setExperienceContext(ctx);

		// Payment source
		PaymentSource ps = new PaymentSource();
		ps.setPaypal(paypal);

		// Final order request
		OrderRequest order = new OrderRequest();
		order.setIntent(Constant.INTENT_CAPTURE);
		order.setPurchaseUnits(Collections.singletonList(unit));
		order.setPaymentSource(ps);

		log.info("Constructed OrderRequest object: {}", order);

		// Convert to JSON string
		String requestAsJson = jsonUtil.toJson(order);
		return requestAsJson;
	}

	private HttpHeaders prepareHeader(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// set header PayPal-Request-Id => UUID
		String uuid = UUID.randomUUID().toString();
		log.info("Generated UUID for PayPal-Request-Id: {}", uuid);

		headers.add(Constant.PAY_PAL_REQUEST_ID, uuid);
		return headers;
	}
	
	public OrderResponse toOrderResponse(PaypalOrder paypalOrder) {
		log.info("Converting PaypalOrder to OrderResponse: {}", paypalOrder);
		
	    OrderResponse response = new OrderResponse();
	    response.setId(paypalOrder.getId());
	    response.setStatus(paypalOrder.getStatus());

	    String redirectLink = paypalOrder.getLinks().stream()
	            .filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
	            .findFirst()
	            .map(PaypalLink::getHref)
	            .orElse(null);

	    response.setRedirectUrl(redirectLink);
	    
	    log.info("Converted PaypalOrder to OrderResponse: {}", response);

	    return response;
	}
	
	public OrderResponse handlePaypalResponse(ResponseEntity<String> httpResponse) {
		log.info("Handling PayPal response in PaymentServiceImpl "
				+ "httpResponse:{}", httpResponse);
		
		if(httpResponse.getStatusCode().is2xxSuccessful()) { //success

			PaypalOrder paypalOrder = jsonUtil.fromJson(
					httpResponse.getBody(), PaypalOrder.class);
			log.info("Converted response body to PaypalOrder: {}", paypalOrder);
			
			OrderResponse orderResponse = toOrderResponse(paypalOrder);
			log.info("Converted OrderResponse: {}", orderResponse);
			
			// If we get a valid response with PAYER_ACTION_REQUIRED status & url & id, then only its success else its failed.
			if(orderResponse != null 
					&& orderResponse.getId() != null
					&& !orderResponse.getId().isEmpty()
					&& orderResponse.getStatus() != null
					&& orderResponse.getStatus().equalsIgnoreCase(
							Constant.PAYER_ACTION_REQUIRED)
					&& orderResponse.getRedirectUrl() != null
					&& !orderResponse.getRedirectUrl().isEmpty()) {
				log.info("Order created successfully with PAYER_ACTION_REQUIRED status");
				return orderResponse;
			}
			
			log.error("Order creation failed or incomplete details received. "
					+ "orderResponse: {}", orderResponse);
			
		}
		
		// if 4xx or 5xx then proper error
		if(httpResponse.getStatusCode().is4xxClientError() 
				|| httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Received 4xx, 5xx error response from PayPal service");
			
			PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(
					httpResponse.getBody(), PaypalErrorResponse.class);
			log.info("PayPal error response details: {}", paypalErrorRes);
			
			String errorCode = ErrorCodeEnum.PAYPAL_ERROR.getErrorCode();
			String errorMessage = PaypalOrderUtil.getPaypalErrorSummary(
					paypalErrorRes);
			log.info("Generated PayPal error summary: {}", errorMessage);
			
			throw new PaypalProviderException(
					errorCode,
					errorMessage,
					HttpStatus.valueOf(
							httpResponse.getStatusCode().value()));
		}
		

		log.error("Unexpected response from PayPal service. "
				+ "httpResponse: {}", httpResponse);
		
		throw new PaypalProviderException(
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(),
				HttpStatus.BAD_GATEWAY);
	}

}
