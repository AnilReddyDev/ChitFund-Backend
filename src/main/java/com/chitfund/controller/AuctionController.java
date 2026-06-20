package com.chitfund.controller;

import com.chitfund.entity.Auction;
import com.chitfund.service.AuctionService;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auction")
@Validated
public class AuctionController {

    private final AuctionService service;

    public AuctionController(AuctionService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Auction create(
            @RequestParam @Positive Long groupId,
            @RequestParam @Positive Integer month,
            @RequestParam @Positive Long winnerId,
            @RequestParam @Positive Double bidAmount
    ) {
        return service.createAuction(groupId, month, winnerId, bidAmount);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<Auction> getHistory(@PathVariable @Positive Long groupId) {
        return service.getHistory(groupId);
    }
}
