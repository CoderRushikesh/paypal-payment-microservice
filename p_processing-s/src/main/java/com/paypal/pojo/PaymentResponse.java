package com.paypal.pojo;

import lombok.Data;

@Data
public class PaymentResponse {

	
	private String txnReference;
	private int txnStatusId;
	
	private String redirectUrl;
	private String providerReference;
	
}
