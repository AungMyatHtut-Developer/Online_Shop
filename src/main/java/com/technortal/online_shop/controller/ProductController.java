package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.ProductDto;
import com.technortal.online_shop.dto.ProductFormDto;
import com.technortal.online_shop.dto.ValidationErrorDto;
import com.technortal.online_shop.exception.ProductNotFoundException;
import com.technortal.online_shop.exception.ProductValidationException;
import com.technortal.online_shop.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.UUID;

@Controller
public class ProductController {

    static final String PRODUCT_TOKEN = "productToken";
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @InitBinder("product")
    public void bindProductFields(WebDataBinder binder) {
        // Image bytes come only from the multipart upload, never from bound form fields.
        binder.setAllowedFields("productName", "price", "quantity", "removeImage");
    }

    @GetMapping("/products")
    public String showProducts(HttpServletRequest request, Model model) {
        if (!isLoggedIn(request)) return "redirect:/login";
        addProductToken(request, model);
        model.addAttribute("products", productService.getProducts());
        return "index";
    }

    @GetMapping("/products/new")
    public String showNewProduct(HttpServletRequest request, Model model) {
        if (!isLoggedIn(request)) return "redirect:/login";
        model.addAttribute("product", new ProductFormDto());
        return showForm(request, model, null, false);
    }

    @GetMapping("/products/{id}/edit")
    public String showEditProduct(@PathVariable Long id, HttpServletRequest request, Model model) {
        if (!isLoggedIn(request)) return "redirect:/login";
        ProductDto product = productService.getProduct(id);
        model.addAttribute("product", product);
        return showForm(request, model, id, product.isImageAvailable());
    }

    @PostMapping("/products")
    public String createProduct(@ModelAttribute("product") ProductFormDto product, BindingResult errors,
                                @RequestParam(required = false) MultipartFile image,
                                @RequestParam(required = false) String productToken,
                                HttpServletRequest request, Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(request)) return "redirect:/login";
        checkProductToken(request, productToken);
        bindImageUpload(product, image, errors);
        if (errors.hasErrors()) return showForm(request, model, null, false);

        try {
            productService.createProduct(product);
        } catch (ProductValidationException ex) {
            addValidationErrors(errors, ex);
            return showForm(request, model, null, false);
        }
        redirect.addFlashAttribute("success", "Product created successfully.");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") ProductFormDto form, BindingResult errors,
                                @RequestParam(required = false) MultipartFile image,
                                @RequestParam(required = false) String productToken,
                                HttpServletRequest request, Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(request)) return "redirect:/login";
        checkProductToken(request, productToken);
        bindImageUpload(form, image, errors);
        if (errors.hasErrors()) return showInvalidEditForm(request, model, id);

        try {
            productService.updateProduct(id, form);
        } catch (ProductValidationException ex) {
            addValidationErrors(errors, ex);
            return showInvalidEditForm(request, model, id);
        }
        redirect.addFlashAttribute("success", "Product updated successfully.");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @RequestParam(required = false) String productToken,
                                HttpServletRequest request, RedirectAttributes redirect) {
        if (!isLoggedIn(request)) return "redirect:/login";
        checkProductToken(request, productToken);
        productService.deleteProduct(id);
        redirect.addFlashAttribute("success", "Product deleted successfully.");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> showProductImage(@PathVariable Long id, HttpServletRequest request) {
        if (!isLoggedIn(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return productService.getProductImage(id)
                .map(image -> ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getContentType()))
                        .cacheControl(CacheControl.noStore()).header("X-Content-Type-Options", "nosniff")
                        .body(image.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Void> handleProductNotFound() {
        return ResponseEntity.notFound().build();
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(LoginController.LOGGED_IN_USER) != null;
    }

    private void addProductToken(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session.getAttribute(PRODUCT_TOKEN) == null) {
            session.setAttribute(PRODUCT_TOKEN, UUID.randomUUID().toString());
        }
        model.addAttribute(PRODUCT_TOKEN, session.getAttribute(PRODUCT_TOKEN));
    }

    private void checkProductToken(HttpServletRequest request, String token) {
        if (token == null || !token.equals(request.getSession(false).getAttribute(PRODUCT_TOKEN))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please reload the page and try again.");
        }
    }

    private String showForm(HttpServletRequest request, Model model, Long id, boolean hasImage) {
        addProductToken(request, model);
        model.addAttribute("productId", id);
        model.addAttribute("hasImage", hasImage);
        return "product-form";
    }

    private String showInvalidEditForm(HttpServletRequest request, Model model, Long id) {
        ProductDto product = productService.getProduct(id);
        return showForm(request, model, id, product.isImageAvailable());
    }

    private void addValidationErrors(BindingResult errors, ProductValidationException exception) {
        for (ValidationErrorDto error : exception.getErrors()) {
            if (error.field() == null) errors.reject(error.code(), error.message());
            else errors.rejectValue(error.field(), error.code(), error.message());
        }
    }

    private void bindImageUpload(ProductFormDto product, MultipartFile image, BindingResult errors) {
        if (image == null || image.isEmpty()) return;
        try {
            product.setProductImage(image.getBytes());
        } catch (IOException ex) {
            errors.reject("imageRead", "The image could not be read. Please choose it again.");
        }
    }
}
