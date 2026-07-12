package com.synapse.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // This triggers the scan of com.synapse.helpdesk.*
public class HelpdeskApplication {
	public static void main(String[] args) {
		SpringApplication.run(HelpdeskApplication.class, args);
	}
}