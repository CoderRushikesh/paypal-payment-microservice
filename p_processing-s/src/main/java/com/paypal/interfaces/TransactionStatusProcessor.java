package com.paypal.interfaces;

import com.paypal.dto.TransactionDto;

public interface TransactionStatusProcessor {

	public TransactionDto processStatus(TransactionDto tnxDto);
	
}
