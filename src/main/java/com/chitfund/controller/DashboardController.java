package com.chitfund.controller;

import com.chitfund.dto.ChartDataDTO;
import com.chitfund.dto.DashboardResponse;
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

    @GetMapping("/chart")
    public List<ChartDataDTO> chart(@RequestParam Long groupId) {
        return service.getChartData(groupId);
    }
}