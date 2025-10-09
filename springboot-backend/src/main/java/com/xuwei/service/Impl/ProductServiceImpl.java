package com.xuwei.service.Impl;

import com.xuwei.exception.ProductException;
import com.xuwei.model.Category;
import com.xuwei.model.Product;
import com.xuwei.model.Seller;
import com.xuwei.repository.CategoryRepository;
import com.xuwei.repository.ProductRepository;
import com.xuwei.request.CreateProductRequest;
import com.xuwei.service.ProductService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(CreateProductRequest request, Seller seller) {
        //  Build category hierarchy (Level 1 → 2 → 3)
        Category category = ensureCategoryExists(request.getCategory(), 1, null);
        Category subCategory = ensureCategoryExists(request.getSubCategory(), 2, category);
        Category subSubCategory = ensureCategoryExists(request.getSubSubCategory(), 3, subCategory);

        //  Calculate discount percentage
        int discountPercentage = calculateDiscountPercentage(request.getMrpPrice(), request.getSellingPrice());

        //  Build and save new product
        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(subSubCategory);
        product.setTitle(request.getTitle());
        product.setColor(request.getColor());
        product.setDescription(request.getDescription());
        product.setDiscountPercent(discountPercentage);
        product.setSellingPrice(request.getSellingPrice());
        product.setImages(request.getImages());
        product.setMrpPrice(request.getMrpPrice());
        product.setSizes(request.getSizes());
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     *  Utility method: Ensure category exists or create a new one.
     */
    private Category ensureCategoryExists(String categoryId, int level, Category parent) {
        if (categoryId == null || categoryId.isEmpty()) return null;

        Category category = categoryRepository.findByCategoryId(categoryId);
        if (category == null) {
            Category newCategory = new Category();
            newCategory.setCategoryId(categoryId);
            newCategory.setLevel(level);
            newCategory.setPercentCategory(parent);
            category = categoryRepository.save(newCategory);
        }
        return category;
    }

    /**
     *  Utility method: Calculate discount percentage based on MRP and selling price.
     */
    public static int calculateDiscountPercentage(double mrpPrice, double sellingPrice) {
        if (mrpPrice <= 0) {
            throw new IllegalArgumentException("Actual price must be greater than zero.");
        }
        double discount = mrpPrice - sellingPrice;
        return (int) ((discount / mrpPrice) * 100);
    }

    @Override
    public void deleteProduct(Long productId) throws ProductException {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) {
        //  Ensure product exists before updating
        productRepository.findById(productId);
        product.setId(productId);
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long productId) throws ProductException {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException("Product not found"));
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    @Override
    public Page<Product> getAllProducts(
            String category,
            String brand,
            String color,
            String sizes,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber
    ) {
        //  Build Specification dynamically (filtering logic)
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                Join<Product, Category> categoryJoin = root.join("category");
                Predicate categoryPredicate = cb.or(
                        cb.equal(categoryJoin.get("categoryId"), category),
                        cb.equal(categoryJoin.get("parentCategory").get("categoryId"), category)
                );
                predicates.add(categoryPredicate);
            }

            if (color != null && !color.isEmpty()) {
                predicates.add(cb.equal(root.get("color"), color));
            }

            if (sizes != null && !sizes.isEmpty()) {
                predicates.add(cb.equal(root.get("size"), sizes));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }

            if (minDiscount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("discountPercent"), minDiscount));
            }

            if (stock != null) {
                predicates.add(cb.equal(root.get("stock"), stock));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = buildPageable(pageNumber, sort);
        return productRepository.findAll(spec, pageable);
    }

    /**
     *  Utility method: Build Pageable object based on sort and page number.
     */
    private Pageable buildPageable(Integer pageNumber, String sort) {
        int page = (pageNumber != null) ? pageNumber : 0;
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, 10, Sort.unsorted());
        }
        return switch (sort) {
            case "price_low" -> PageRequest.of(page, 10, Sort.by("sellingPrice").ascending());
            case "price_high" -> PageRequest.of(page, 10, Sort.by("sellingPrice").descending());
            default -> PageRequest.of(page, 10, Sort.unsorted());
        };
    }

    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}
