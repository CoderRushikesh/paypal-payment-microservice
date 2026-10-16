package com.paypal.service.interfaces;

import com.paypal.pojo.CreatePaymentRequest;
import com.paypal.pojo.InitiatePaymentRequest;
import com.paypal.pojo.PaymentResponse;

public interface PaymentService {
	
	public PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest);
	
	public PaymentResponse initiatePayment(String txnReference, 
			InitiatePaymentRequest initiatePaymentRequest);
	
	public PaymentResponse capturePayment(String txnReference);

}