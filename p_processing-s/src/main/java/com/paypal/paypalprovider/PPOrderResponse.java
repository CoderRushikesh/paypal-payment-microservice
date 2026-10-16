package com.paypal.paypalprovider;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PPOrderResponse {

	@JsonProperty("id")
	private String orderId;
	private String PaypalStatus;
	private String redirectUrl;
	
	
	
}
