package com.payment.config;



import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI paypalProviderOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("PayPal Provider Service API")
				.version("v1.0.0")
				.summary("Secure gateway between our platform and the PayPal Orders API")
				.description("""
						## Overview
						Part of a **PayPal payment integration system** made of two microservices:

						| Service | Responsibility |
						|---|---|
						| **paypal_processing** | Orchestrates payments and handles **payment status tracking** |
						| **paypal_provider** (this service) | **Securely communicates with the PayPal REST API**: OAuth2 token handling, create order, capture order |

						## How to test the full payment flow (PayPal Sandbox)

						**Step 1: Create the order**
						Call `POST /Payments`. Copy `id` and `redirectUrl` from the response.

						**Step 2: Get a sandbox buyer account (one-time setup)**
						1. Go to https://developer.paypal.com and log in
						2. Open **Testing tools > Sandbox accounts**
						3. Use the **Personal** (buyer) account, or create one
						4. Click **View/Edit account** to see its email and password

						**Step 3: Approve the payment**
						1. Open `redirectUrl` in a browser
						2. Log in with the **sandbox buyer** credentials (not your developer login)
						3. Click **Continue / Pay Now**
						4. The browser redirects to your `returnUrl` with `?token=<orderId>&PayerID=...`.
						   The page may show an error if it doesn't exist. That is fine, just copy the `token`.

						**Step 4: Capture the payment**
						Call `POST /{orderId}/capture` using the order id. Status becomes `COMPLETED`.

						> Calling capture before Step 3 returns `422 ORDER_NOT_APPROVED`.
						> Calling capture twice returns `422` (already captured).

						## Error format
						Every failing endpoint returns `{ "errorCode": "...", "errorMessage": "..." }`.
						PayPal 4xx/5xx errors keep their original HTTP status. Provider outages return `503`, unexpected PayPal responses return `502`.
						""")
				.contact(new Contact()
					.name("Your Name")
					.email("your.email@example.com")
					.url("https://github.com/your-username"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0")))
			.externalDocs(new ExternalDocumentation()
				.description("PayPal Orders API v2 Reference")
				.url("https://developer.paypal.com/docs/api/orders/v2/"))
			.servers(List.of(
				new Server().url("http://localhost:8083").description("Local")))
			.tags(List.of(
				new Tag().name("Payments")
					.description("Create and capture PayPal orders")));
	}
}