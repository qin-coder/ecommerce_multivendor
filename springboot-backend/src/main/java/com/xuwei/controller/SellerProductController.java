package com.xuwei.controller;

import com.xuwei.exception.ProductException;
import com.xuwei.exception.SellerException;
import com.xuwei.model.Product;
import com.xuwei.model.Seller;
import com.xuwei.request.CreateProductRequest;
import com.xuwei.service.ProductService;
import com.xuwei.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;
    private final SellerService sellerService;


    @GetMapping()
    public ResponseEntity<List<Product>> getProductBySellerId(
            @RequestHeader("Authorization") String jwt) throws ProductException, SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);

        List<Product> products =
                productService.getProductBySellerId(seller.getId());
        return new ResponseEntity<>(products, HttpStatus.OK);

    }

    @PostMapping()
    public ResponseEntity<Product> createProduct(
            @RequestBody CreateProductRequest request,

            @RequestHeader("Authorization") String jwt) {

        Seller seller = sellerService.getSellerProfile(jwt);

        Product product = productService.createProduct(request,
                seller);
        return new ResponseEntity<>(product, HttpStatus.CREATED);

    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        try {
            productService.deleteProduct(productId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ProductException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody CreateProductRequest request,
            @RequestHeader("Authorization") String jwt) throws ProductException, SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);
        Product updatedProduct = productService.updateProduct(productId, request, seller);

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }




}
