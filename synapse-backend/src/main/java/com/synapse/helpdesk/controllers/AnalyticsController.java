package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.AnalyticsDto;
import com.synapse.helpdesk.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<AnalyticsDto> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}