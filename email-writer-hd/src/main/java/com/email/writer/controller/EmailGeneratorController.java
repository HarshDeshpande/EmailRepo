package com.email.writer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.email.writer.model.EmailRequest;
import com.email.writer.service.EmailGeneratorService;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins="*")
public class EmailGeneratorController {
	
	public final EmailGeneratorService emailGeneratorService;
	
	public EmailGeneratorController(EmailGeneratorService emailGeneratorService) {
		this.emailGeneratorService = emailGeneratorService;
	}

	@PostMapping("/generator")
	public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest){
		String response = emailGeneratorService.generateEmailReply(emailRequest);
		return ResponseEntity.ok(response);
	}
}
