package com.payment.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.http.HttpRequest;
import com.payment.http.HttpServiceEngine;
import com.payment.res.PaypalOAuthToken;
import com.payment.util.JsonUtil;
import com.paymentl.Constant.Constant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {	

	private final HttpServiceEngine httpServiceEngine;

	// TODO , implement caching mechanism for access token take care for expiry
	private static String accessToken;

	@Value("${paypal.client.id}")
	private String CLIENT_ID;

	@Value("${paypal.client.secret}")    // ← fix this
	private String CLIENT_SECRET;

	@Value("${paypal.oauth.url}")        // ← fix this
	private String oauthUrl;
	
	private final ObjectMapper objectMapper;
	private final JsonUtil jsonUtil;

	public String getAccessToken() 
	{

		 String clientId = CLIENT_ID.trim();
		    String clientSecret = CLIENT_SECRET.trim();
		  log.info("CLIENT_ID value: [{}]", CLIENT_ID);
		    log.info("CLIENT_SECRET value: [{}]", CLIENT_SECRET);
		    log.info("OAuth URL: [{}]", oauthUrl);
		
		log.info("Getting access token from PayPal");

		if(accessToken != null) 
		{
			log.info("Returning cached access token");
			return accessToken;
		}

		log.info("No cached access token found, calling OAuth service ");
	
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET); // generates Authorization header
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Prepare form data
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add(Constant.GRANT_TYPE, Constant.CLIENT_CREDENTIALS );


		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(oauthUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(formData);


		log.info("Prepared HTTP Request for OAuth token: {}", httpRequest);

		ResponseEntity<String> response = httpServiceEngine.makeHttpRequest(httpRequest);
		log.info("HTTP Response received: {}", response);
		
		String tokenBody = response.getBody();
		
		
		PaypalOAuthToken token=   jsonUtil.fromJson(tokenBody, PaypalOAuthToken.class);
		 accessToken = token.getAccessToken();
		   log.info("Caching access Token for future use");
	    
	    
//		try {
//			PaypalOAuthToken token = objectMapper.readValue(tokenBody, PaypalOAuthToken.class);
//		
//		   log.info("Parsed OAuth token response: {}", token);
//		   
//		   accessToken = token.getAccessToken();
//		   log.info("Caching access Token for future use");
//		   
//		   return token.getAccessToken();
//		} catch (Exception e) {
//			log.error("Error parsing OAuth token response", e);
//			throw new RuntimeException("Failed to parse OAuth token response", e);
//		} 

	  return accessToken;
	}

}
