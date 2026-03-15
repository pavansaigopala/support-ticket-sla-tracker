package com.inu.sts.support_ticket_sla_tracker;

import com.inu.sts.support_ticket_sla_tracker.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RateLimitProperties.class)
public class SupportTicketSlaTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportTicketSlaTrackerApplication.class, args);
	}

}
