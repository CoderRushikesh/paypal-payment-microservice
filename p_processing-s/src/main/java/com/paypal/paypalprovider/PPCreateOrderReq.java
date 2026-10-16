package com.paypal.paypalprovider;

import lombok.Data;

@Data
public class PPCreateOrderReq {

	 private String currencyCode;
	 private Double amount;
//	 private String currency;
	 private String returnUrl;
	 private String cancelUrl;
	 
	
}
