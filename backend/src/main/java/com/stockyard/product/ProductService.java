package com.stockyard.product;

import com.stockyard.auth.MakerRepository;
import com.stockyard.common.ApiException;
import com.stockyard.common.ImageStorage;
import com.stockyard.product.ProductDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final MakerRepository makers;
    private final ImageStorage images;

    public ProductService(ProductRepository products, MakerRepository makers, ImageStorage images) {
        this.products = products;
        this.makers = makers;
        this.images = images;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(Long makerId, boolean archived) {
        return products.findByMakerIdAndArchivedOrderByTitleAsc(makerId, archived).stream()
                .map(ProductResponse::of).toList();
    }

    public ProductResponse create(Long makerId, ProductRequest req) {
        String sku = req.sku().trim();
        if (products.existsByMakerIdAndSkuIgnoreCase(makerId, sku)) {
            throw ApiException.conflict("SKU \"" + sku + "\" is already in use");
        }
        Product p = new Product(makers.getReferenceById(makerId));
        apply(p, req);
        return ProductResponse.of(products.save(p));
    }

    public ProductResponse update(Long makerId, Long id, ProductRequest req) {
        Product p = find(makerId, id);
        String sku = req.sku().trim();
        if (!p.getSku().equalsIgnoreCase(sku) && products.existsByMakerIdAndSkuIgnoreCase(makerId, sku)) {
            throw ApiException.conflict("SKU \"" + sku + "\" is already in use");
        }
        apply(p, req);
        return ProductResponse.of(p);
    }

    public ProductResponse setArchived(Long makerId, Long id, boolean archived) {
        Product p = find(makerId, id);
        p.setArchived(archived);
        return ProductResponse.of(p);
    }

    public ProductResponse uploadImage(Long makerId, Long id, MultipartFile file) {
        Product p = find(makerId, id);
        p.setImageUrl(images.store(file));
        return ProductResponse.of(p);
    }

    private Product find(Long makerId, Long id) {
        return products.findByIdAndMakerId(id, makerId).orElseThrow(() -> ApiException.notFound("Product"));
    }

    private void apply(Product p, ProductRequest req) {
        p.setSku(req.sku().trim());
        p.setTitle(req.title().trim());
        p.setImageUrl(req.imageUrl() == null || req.imageUrl().isBlank() ? p.getImageUrl() : req.imageUrl().trim());
        p.setUnitCost(req.unitCost());
        p.setRetailPrice(req.retailPrice());
        p.setStock(req.stock());
        if (req.lowStockThreshold() != null) p.setLowStockThreshold(req.lowStockThreshold());
    }
}
