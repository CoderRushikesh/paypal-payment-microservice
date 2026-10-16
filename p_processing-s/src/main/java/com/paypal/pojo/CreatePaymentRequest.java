package com.paypal.pojo;

import lombok.Data;

@Data
public class CreatePaymentRequest {

	private String userId;
	private String paymentMethodId;
	
	private int providerId;
	private int paymentTypeId;
	
	private double amount;
	private String currency;
	
	private String merchantTransactionReference;
	
	
	
}
