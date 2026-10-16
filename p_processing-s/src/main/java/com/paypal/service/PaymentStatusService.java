package com.paypal.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.paypal.constant.ErrorCodeEnum;
import com.paypal.dto.TransactionDto;
import com.paypal.exception.ProcessingServiceException;
import com.paypal.interfaces.TransactionStatusProcessor;
import com.paypal.service.factory.PaymentStatusFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusService {
 
	private final PaymentStatusFactory paymentStatusFactory;
	
	
	 public TransactionDto processPayment(TransactionDto txnDto) {
		 
		 log.info("Processing payment status for txnDto : {}" , txnDto);
		 int statusId = txnDto.getTxnStatusId();
		 TransactionStatusProcessor	 processor = paymentStatusFactory.getStatusProcessor(statusId);
	    
		 if(processor == null) {
			 
			 log.error("No processor found for statusId : {}", statusId);
			 throw new ProcessingServiceException(ErrorCodeEnum.NO_STATUS_PROCESSOR_FOUND.getErrorCode() , ErrorCodeEnum.NO_STATUS_PROCESSOR_FOUND.getErrorMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
			 
		 }
		 
	   TransactionDto response = processor.processStatus(txnDto);
	   log.info("Processed payment status for statusId : {} with response: {}", statusId, response);
	 
	  return response;
	 }
	
	
	
}
