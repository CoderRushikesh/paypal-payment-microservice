package com.paypal.service.processor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.paypal.dao.interfaces.TransactionDao;
import com.paypal.dto.TransactionDto;
import com.paypal.entity.TransactionEntity;
import com.paypal.interfaces.TransactionStatusProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PendingStatusProcessor implements TransactionStatusProcessor {

	private final TransactionDao transactionDao;

	private final ModelMapper modelMapper;

	@Override
	public TransactionDto processStatus(TransactionDto txnDto) {
		log.info("Processing 'PENDING' status for txnDto: {}", txnDto);
		
		// convert DTO to Entity
		TransactionEntity txnEntity = modelMapper.map(
				txnDto, TransactionEntity.class);
		
		transactionDao.updateTransaction(txnEntity);
		log.info("Updated TransactionEntity in DB for PENDING status: {}", txnEntity);

		return txnDto;
	}

}