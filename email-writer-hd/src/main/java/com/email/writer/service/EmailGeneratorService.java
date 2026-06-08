package com.email.writer.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.email.writer.model.EmailRequest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class EmailGeneratorService {
	
	private final WebClient webClient;
	
	public EmailGeneratorService(WebClient.Builder webClientBuilder) {
		super();
		this.webClient = webClientBuilder.build();
	}

	@Value("${gemini.api.url}")
	private String geminiApiUrl;
	
	@Value("${gemini.api.key}")
	private String geminiApiKey;

	public String generateEmailReply(EmailRequest emailRequest) {
		//Build the prompt
		String prompt = buildPrompt(emailRequest);
		
		// Craft a request
		Map<String, Object> requestBody = Map.of(
				"contents",new Object[] {
						Map.of("parts",new Object[] {
								Map.of("text",prompt)
						})
				}
				);
		
		// Do request and get response
		 String response = webClient.post()
				 .uri(geminiApiUrl + geminiApiKey)
				 .header("Content-Type","application/json")
				 .bodyValue(requestBody)
				 .retrieve()
				 .bodyToMono(String.class)
				 .block();
		
		// Extract response and Return 
		return extractResponseContent(response);
	}

	private String extractResponseContent(String response) {
		try {
			//ObjectMapper => From jackson library helps to work with JSON data
			//Read,write and convert JSON data into java object and vice versa
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(response);
			return rootNode.path("candidates")
					.get(0)
					.path("content")
					.path("parts")
					.get(0)
					.path("text")
					.asText();
		}catch(Exception e) {
			return "Error processing request :"+e.getMessage();
		}
	}

	private String buildPrompt(EmailRequest emailRequest) {
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate a single professional email reply based on the following email. ");
		prompt.append("Do not generate multiple options. ");
		prompt.append("Do not include a subject line. ");
		prompt.append("Keep it clear and concise. ");
		if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
			prompt.append("Use a ").append(emailRequest.getTone()).append(" tone" );
		}
		prompt.append("\nOriginal Email : \n").append(emailRequest.getEmailContent());
		return prompt.toString();
	}
}
