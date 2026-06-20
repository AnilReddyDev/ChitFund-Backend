package com.chitfund.controller;

import com.chitfund.dto.ChartDataDTO;
import com.chitfund.dto.DashboardResponse;
import com.chitfund.dto.DashboardSummaryResponse;
import com.chitfund.dto.DashboardTrendsResponse;
import com.chitfund.service.DashboardService;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Validated
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public DashboardResponse get(@RequestParam @Positive Long groupId) {
        return service.getDashboard(groupId);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public DashboardSummaryResponse summary(@RequestParam @Positive Long groupId,
                                            @RequestParam(required = false) @Positive Integer month) {
        return service.getSummary(groupId, month);
    }

    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<ChartDataDTO> chart(@RequestParam @Positive Long groupId) {
        return service.getChartData(groupId);
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public DashboardTrendsResponse trends(@RequestParam @Positive Long groupId) {
        return service.getTrends(groupId);
    }
}
