package com.paypal.service;

import java.util.UUID;


import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.paypal.dao.interfaces.TransactionDao;
import com.paypal.dto.TransactionDto;
import com.paypal.entity.TransactionEntity;
import com.paypal.exception.ProcessingServiceException;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpServiceEngine;
import com.paypal.interfaces.PaymentService;
import com.paypal.paypalprovider.PPOrderResponse;
import com.paypal.pojo.CreatePaymentRequest;
import com.paypal.pojo.InitiatePaymentRequest;
import com.paypal.pojo.PaymentResponse;
import com.paypal.service.helper.PPCaptureOrderHelper;
import com.paypal.service.helper.PPCreateOrderHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PPCreateOrderHelper ppCreateOrderHelper;
	
	private final PPCaptureOrderHelper ppCaptureOrderHelper;

	private final HttpServiceEngine httpServiceEngine;

	private final PaymentStatusService paymentStatusService;

	private final ModelMapper modelMapper;
	
	private final TransactionDao transactionDao;

	@Override
	public PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
		log.info("Creating payment in PaymentServiceImpl..."
				+ "||createPaymentRequest:{}",
				createPaymentRequest);

		
		TransactionDto txnDto = modelMapper.map(
				createPaymentRequest, TransactionDto.class);
		log.info("Mapped CreatePaymentRequest to TransactionDto: {}", txnDto);


		int txnStatusId = 1; // CREATED
		String txnReference = generateUniqueTxnReference(); // for every payment, have unique reference
		
		txnDto.setTxnStatusId(txnStatusId);
		txnDto.setTxnReference(txnReference);
		
		TransactionDto response = paymentStatusService.processPayment(txnDto);
		log.info("Response from TransactionStatusProcessor: {}", response);
		
		PaymentResponse paymentRes = new PaymentResponse();
		paymentRes.setTxnReference(response.getTxnReference());
		paymentRes.setTxnStatusId(response.getTxnStatusId());
		log.info("Prepared PaymentResponse: {}", paymentRes);
		
		return paymentRes;
	}

	private String generateUniqueTxnReference() {
		return UUID.randomUUID().toString();
	}

	@Override
	public PaymentResponse initiatePayment(String txnReference, 
			InitiatePaymentRequest initiatePaymentRequest) {
		log.info("Initiating payment in PaymentServiceImpl... "
				+ "txnReference: {} | initiatePaymentRequest:{}", 
				txnReference, initiatePaymentRequest);
		
		TransactionEntity txnEntity = transactionDao.getTransactionByTxnReference(txnReference);
		log.info("Fetched TransactionEntity from DB: {}", txnEntity);
		
		// use modelMapper to convert Entity to DTO
		TransactionDto txnDto = modelMapper.map(
				txnEntity, TransactionDto.class);
		log.info("Mapped TransactionEntity to TransactionDto: {}", txnDto);
		
		// update txn status to INITIATED
		txnDto.setTxnStatusId(2); // INITIATED
		txnDto = paymentStatusService.processPayment(txnDto);
		log.info("Response from PaymentStatusService after updating status to INITIATED: {}", txnDto);
		
		// MAKE API CALL To payal-provider to createOrder API
		/*
		 * 1. Prepare HttpRequest - DONE
		 * 2. Pass to HttpServiceEngine
		 * 3. Process the response
		 */

		HttpRequest httpReq = ppCreateOrderHelper.prepareHttpRequest(
				txnReference, initiatePaymentRequest, txnDto);
		log.info("Prepared HttpRequest for PayPalProvider create order: {}", httpReq);

		PPOrderResponse ppOrderSuccessResponse = null;
		try {
			ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpReq);
			log.info("HTTP response from HttpServiceEngine: {}", httpResponse);
			
			ppOrderSuccessResponse = ppCreateOrderHelper.processResponse(httpResponse);
			log.info("Processed PayPal order response: {}", ppOrderSuccessResponse);
		} catch (ProcessingServiceException e) {
			log.error("Error occurred while making HTTP call to PayPalProvider: ", e);
			
			// update txn status to FAILED
			txnDto.setTxnStatusId(6); // FAILED
			txnDto.setErrorCode(e.getErrorCode());
			txnDto.setErrorMessage(e.getErrorMessage());
			
			paymentStatusService.processPayment(txnDto);
			log.info("Updated transaction status to FAILED for txnReference: {}", txnReference);
			
			throw e; // rethrow the exception after updating status
		}// you can catch Exception, to handle any other unexpected errors. 
		//Create custom errorcode, update to failed status and throw exception. 
		
		
		// update txn status to PENDING
		txnDto.setTxnStatusId(3); // PENDING
		txnDto.setProviderReference(ppOrderSuccessResponse.getOrderId());
		txnDto = paymentStatusService.processPayment(txnDto);
		
		PaymentResponse paymentResponse = new PaymentResponse();
		paymentResponse.setTxnReference(txnDto.getTxnReference());
		paymentResponse.setTxnStatusId(txnDto.getTxnStatusId());
		paymentResponse.setProviderReference(ppOrderSuccessResponse.getOrderId());
		paymentResponse.setRedirectUrl(ppOrderSuccessResponse.getRedirectUrl());

		log.info("Final PaymentResponse to be returned: {}", paymentResponse);
		
		return paymentResponse;
	}

	@Override
	public PaymentResponse capturePayment(String txnReference) {
		log.info("Capturing payment in PaymentServiceImpl... "
				+ "txnReference: {}", txnReference);
		
		TransactionEntity txnEntity = transactionDao.getTransactionByTxnReference(
				txnReference);
		log.info("Fetched TransactionEntity from DB: {}", txnEntity);
		
		// use modelMapper to convert Entity to DTO
		TransactionDto txnDto = modelMapper.map(
				txnEntity, TransactionDto.class);
		log.info("Mapped TransactionEntity to TransactionDto: {}", txnDto);
		
		// update txn status to APPROVED
		txnDto.setTxnStatusId(4);  
		txnDto = paymentStatusService.processPayment(txnDto);
		log.info("Response from PaymentStatusService after updating status to APPROVED: {}", txnDto);
		
		HttpRequest httpReq = ppCaptureOrderHelper.prepareHttpRequest(
				txnReference, txnDto);
		
		PPOrderResponse ppCaptureOrderSuccessResponse = null;
		try {
			ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpReq);
			log.info("HTTP response from HttpServiceEngine: {}", httpResponse);
			
			ppCaptureOrderSuccessResponse = ppCaptureOrderHelper.processResponse(httpResponse);
			log.info("Processed PayPal order response: {}", ppCaptureOrderSuccessResponse);
		} catch (Exception e) {
			log.error("Error occurred while making captureOrder HTTP call to PayPalProvider: ", e);
			
			// Note, dont change the status to FAILED since user already APPROVED.
			// Let reconciliation job handle such cases.
			// In case reconciliation also resolved it as failed, 
			//then manually back-office can handle this payment..
			// just throw error back
			
			throw e; // rethrow the exception after updating status
		} 
		
		// update txn status to SUCCESS
		txnDto.setTxnStatusId(5); 
		txnDto = paymentStatusService.processPayment(txnDto);
		
		PaymentResponse paymentResponse = new PaymentResponse();
		paymentResponse.setTxnReference(txnDto.getTxnReference());
		paymentResponse.setTxnStatusId(txnDto.getTxnStatusId());

		log.info("Final PaymentResponse to be returned: {}", paymentResponse);
		
		return paymentResponse;
	}

}
