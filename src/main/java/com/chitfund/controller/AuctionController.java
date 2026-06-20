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
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_AUCTIONS)")
    public Auction create(
            @RequestParam @Positive Long groupId,
            @RequestParam @Positive Integer month,
            @RequestParam @Positive Long winnerId,
            @RequestParam @Positive Double bidAmount
    ) {
        return service.createAuction(groupId, month, winnerId, bidAmount);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).VIEW_AUCTIONS)")
    public List<Auction> getHistory(@PathVariable @Positive Long groupId) {
        return service.getHistory(groupId);
    }
}
