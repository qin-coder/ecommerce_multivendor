package com.xuwei.service;

import com.xuwei.exception.ProductException;
import com.xuwei.model.Product;
import com.xuwei.model.Seller;
import com.xuwei.request.CreateProductRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Product createProduct(CreateProductRequest request , Seller seller);
    void deleteProduct(Long productId) throws ProductException;
    Product updateProduct(Long productId , Product product);
    Product getProductById(Long productId) throws ProductException;

    List<Product> searchProducts(String query);

    Page<Product> getAllProducts(
            String category,
            String brand,
            String colors,
            String sizes,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber
    );
    List<Product> getProductBySellerId(Long sellerId);

}
