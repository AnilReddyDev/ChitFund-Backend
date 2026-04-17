package com.chitfund.controller;

import com.chitfund.entity.Auction;
import com.chitfund.service.AuctionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auction")
public class AuctionController {

    private final AuctionService service;

    public AuctionController(AuctionService service) {
        this.service = service;
    }

    @PostMapping
    public Auction create(
            @RequestParam Long groupId,
            @RequestParam Integer month,
            @RequestParam Long winnerId,
            @RequestParam Double bidAmount
    ) {
        return service.createAuction(groupId, month, winnerId, bidAmount);
    }

    @GetMapping("/{groupId}")
    public List<Auction> getHistory(@PathVariable Long groupId) {
        return service.getHistory(groupId);
    }
}