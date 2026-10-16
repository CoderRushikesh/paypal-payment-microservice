package com.paypal.dao.interfaces;

import com.paypal.entity.TransactionEntity;

public interface TransactionDao {

	
	public TransactionEntity createTransaction(TransactionEntity transaction );
	public TransactionEntity getTransactionByTxnReference(String txnReference);
	
	public void updateTransaction(TransactionEntity transaction);
}
