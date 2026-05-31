package com.chitfund.controller;

import com.chitfund.dto.ChartDataDTO;
import com.chitfund.dto.DashboardResponse;
import com.chitfund.dto.DashboardSummaryResponse;
import com.chitfund.dto.DashboardTrendsResponse;
import com.chitfund.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse get(@RequestParam Long groupId) {
        return service.getDashboard(groupId);
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@RequestParam Long groupId,
                                            @RequestParam(required = false) Integer month) {
        return service.getSummary(groupId, month);
    }

    @GetMapping("/chart")
    public List<ChartDataDTO> chart(@RequestParam Long groupId) {
        return service.getChartData(groupId);
    }

    @GetMapping("/trends")
    public DashboardTrendsResponse trends(@RequestParam Long groupId) {
        return service.getTrends(groupId);
    }
}
