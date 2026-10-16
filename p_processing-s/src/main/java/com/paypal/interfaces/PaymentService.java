package com.paypal.interfaces;

import com.paypal.dto.TransactionDto;
import com.paypal.pojo.CreatePaymentRequest;
import com.paypal.pojo.InitiatePaymentRequest;
import com.paypal.pojo.PaymentResponse;

public interface PaymentService {

	public PaymentResponse createPayment(CreatePaymentRequest request);
	
	public PaymentResponse initiatePayment(String tnxReference , InitiatePaymentRequest initiatePaymentRequest);
	
	public PaymentResponse capturePayment(String tnxReference);
	
	
}
