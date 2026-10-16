package com.paypal.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.paypal.constant.ErrorCodeEnum;
import com.paypal.dao.interfaces.TransactionDao;
import com.paypal.entity.TransactionEntity;
import com.paypal.exception.ProcessingServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TransactionDaoImpl implements TransactionDao {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public TransactionEntity createTransaction(TransactionEntity txnEntity) {
		log.info("Creating TransactionEntity in DB: {}", txnEntity);

		String sql = "INSERT INTO `Transaction` (" +
				"userId, paymentMethodId, providerId, paymentTypeId, txnStatusId, " +
				"amount, currency, merchantTransactionReference, txnReference, providerReference, " +
				"errorCode, errorMessage, retryCount" +
				") VALUES (" +
				":userId, :paymentMethodId, :providerId, :paymentTypeId, :txnStatusId, " +
				":amount, :currency, :merchantTransactionReference, :txnReference, :providerReference, " +
				":errorCode, :errorMessage, :retryCount" +
				")";

		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(txnEntity);

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

		// Extract generated ID
		Number generatedId = keyHolder.getKey();
		if (generatedId != null) {
			txnEntity.setId(generatedId.intValue());
		}

		log.info("Inserted TransactionEntity with ID: {}", txnEntity.getId());
		return txnEntity;
	}

	@Override
	public TransactionEntity getTransactionByTxnReference(String txnReference) {
		String sql = "SELECT * FROM `Transaction` WHERE txnReference = :txnReference LIMIT 1";

		Map<String, Object> params = new HashMap<>();
		params.put("txnReference", txnReference);

		TransactionEntity txnEntity = jdbcTemplate.queryForObject(
				sql,
				params,
				new BeanPropertyRowMapper<>(TransactionEntity.class)
				);

		log.info("Fetched TransactionEntity by txnReference {}: {}", txnReference, txnEntity);
		return txnEntity;
	}

	@Override
	public void updateTransaction(TransactionEntity transaction) {
		
	    String sql = "UPDATE `Transaction` " +
	            "SET txnStatusId = :txnStatusId, " +
	            "    providerReference = :providerReference, " +
	            "    errorCode = :errorCode, " +
	            "    errorMessage = :errorMessage " +
	            "WHERE id = :id";
	    
		Map<String, Object> params = new HashMap<>();
		params.put("txnStatusId", transaction.getTxnStatusId());
		params.put("providerReference", transaction.getProviderReference());
		params.put("errorCode", transaction.getErrorCode());
	    params.put("errorMessage", transaction.getErrorMessage());
		params.put("id", transaction.getId());

		int rowsAffected = jdbcTemplate.update(sql, params);
		log.info("Updated TransactionEntity ID {}: rows affected = {}", 
				transaction.getId(), rowsAffected);

		if (rowsAffected == 0) {
			log.error("No rows updated for TransactionEntity ID {}", 
					transaction.getId());
			throw new ProcessingServiceException(
					ErrorCodeEnum.ERROR_UPDATING_TRANSACTION.getErrorCode(),
					ErrorCodeEnum.ERROR_UPDATING_TRANSACTION.getErrorMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
