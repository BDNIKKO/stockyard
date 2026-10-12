package com.stockyard.product;

import com.stockyard.product.ProductDtos.*;
import com.stockyard.security.MakerPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> list(@AuthenticationPrincipal MakerPrincipal me,
                                      @RequestParam(defaultValue = "false") boolean archived) {
        return service.list(me.id(), archived);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@AuthenticationPrincipal MakerPrincipal me, @Valid @RequestBody ProductRequest req) {
        return service.create(me.id(), req);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@AuthenticationPrincipal MakerPrincipal me, @PathVariable Long id,
                                  @Valid @RequestBody ProductRequest req) {
        return service.update(me.id(), id, req);
    }

    @PostMapping("/{id}/archive")
    public ProductResponse archive(@AuthenticationPrincipal MakerPrincipal me, @PathVariable Long id) {
        return service.setArchived(me.id(), id, true);
    }

    @PostMapping("/{id}/restore")
    public ProductResponse restore(@AuthenticationPrincipal MakerPrincipal me, @PathVariable Long id) {
        return service.setArchived(me.id(), id, false);
    }

    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ProductResponse uploadImage(@AuthenticationPrincipal MakerPrincipal me, @PathVariable Long id,
                                       @RequestParam("file") MultipartFile file) {
        return service.uploadImage(me.id(), id, file);
    }
}
