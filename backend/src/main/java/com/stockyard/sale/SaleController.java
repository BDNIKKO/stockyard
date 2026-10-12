package com.stockyard.sale;

import com.stockyard.sale.SaleDtos.*;
import com.stockyard.security.MakerPrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @GetMapping
    public List<SaleResponse> list(@AuthenticationPrincipal MakerPrincipal me,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                   @RequestParam(required = false) Long channelId) {
        return service.list(me.id(), from, to, channelId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse record(@AuthenticationPrincipal MakerPrincipal me, @Valid @RequestBody SaleRequest req) {
        return service.record(me.id(), req);
    }
}
