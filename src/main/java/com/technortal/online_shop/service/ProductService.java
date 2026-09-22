package com.technortal.online_shop.service;

import com.technortal.online_shop.dao.ProductDao;
import com.technortal.online_shop.dto.ProductDto;
import com.technortal.online_shop.dto.ProductFormDto;
import com.technortal.online_shop.dto.ProductImageDto;
import com.technortal.online_shop.dto.ValidationErrorDto;
import com.technortal.online_shop.entity.Product;
import com.technortal.online_shop.exception.ProductNotFoundException;
import com.technortal.online_shop.exception.ProductValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");
    private static final Set<String> IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/gif");
    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<ProductDto> getProducts() {
        return productDao.findAllByIsDeleteFalseOrderByIdDesc().stream().map(this::toDto).toList();
    }

    public ProductDto getProduct(Long id) {
        return toDto(findActiveProduct(id));
    }

    public Optional<ProductImageDto> getProductImage(Long id) {
        byte[] image = findActiveProduct(id).getProductImage();
        String contentType = imageContentType(image);
        return contentType == null ? Optional.empty() : Optional.of(new ProductImageDto(image, contentType));
    }

    @Transactional
    public void createProduct(ProductFormDto form) {
        validateProduct(form);
        Product product = new Product();
        applyDetails(product, form);
        productDao.save(product);
    }

    @Transactional
    public void updateProduct(Long id, ProductFormDto form) {
        Product product = findActiveProduct(id);
        // Validate before changing the managed entity, so invalid edits never reach the database.
        validateProduct(form);
        applyDetails(product, form);
        productDao.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findActiveProduct(id);
        product.setDelete(true);
        productDao.save(product);
    }

    private Product findActiveProduct(Long id) {
        return productDao.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(product.getId(), product.getProductName(), product.getPrice(), product.getQuantity(),
                product.getCreatedDate(), product.getUpdatedDate(), product.getProductImage() != null);
    }

    private void applyDetails(Product product, ProductFormDto form) {
        product.setProductName(form.getProductName().strip());
        product.setPrice(form.getPrice());
        product.setQuantity(form.getQuantity());
        byte[] image = form.getProductImage();
        if (image != null && image.length > 0) product.setProductImage(image);
        else if (form.isRemoveImage()) product.setProductImage(null);
    }

    private void validateProduct(ProductFormDto form) {
        List<ValidationErrorDto> errors = new ArrayList<>();
        String name = form.getProductName();
        if (name == null || name.isBlank()) {
            errors.add(new ValidationErrorDto("productName", "required", "Enter a product name."));
        } else if (name.strip().length() > 255) {
            errors.add(new ValidationErrorDto("productName", "length", "Use 255 characters or fewer."));
        }
        BigDecimal price = form.getPrice();
        if (price == null || price.signum() < 0 || price.compareTo(MAX_PRICE) > 0
                || price.stripTrailingZeros().scale() > 2) {
            errors.add(new ValidationErrorDto("price", "range",
                    "Enter a price from 0 to 9999999999.99 with up to 2 decimal places."));
        }
        if (form.getQuantity() == null || form.getQuantity() < 0) {
            errors.add(new ValidationErrorDto("quantity", "range", "Enter a whole quantity of 0 or more."));
        }
        byte[] image = form.getProductImage();
        if (image != null && image.length > 0) {
            if (image.length > MAX_IMAGE_BYTES) {
                errors.add(new ValidationErrorDto(null, "imageSize", "Choose an image no larger than 2 MB."));
            } else if (imageContentType(image) == null) {
                errors.add(new ValidationErrorDto(null, "imageType", "Choose a PNG, JPEG or GIF image."));
            }
        }
        if (!errors.isEmpty()) throw new ProductValidationException(errors);
    }

    private String imageContentType(byte[] image) {
        if (image == null || image.length == 0) return null;
        try {
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));
            return type != null && IMAGE_TYPES.contains(type) ? type : null;
        } catch (IOException ex) {
            return null;
        }
    }
}
