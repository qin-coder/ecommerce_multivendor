package com.xuwei.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String description;
    private int mrpPrice;
    private int sellingPrice;
    private int discountPercent;
    private int quantity;
    private String color;

    @ElementCollection
    private List<String> images = new ArrayList<>();
    private int rating;

    @ManyToOne
    @JsonIgnoreProperties({"products"}) // 只忽略会导致循环的字段
    private Category category;

    @ManyToOne
    @JsonIgnoreProperties({"products"}) // 只忽略会导致循环的字段
    private Seller seller;

    private LocalDateTime createdAt;
    private String sizes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"product"}) // 只忽略会导致循环的字段
    private List<Review> reviews = new ArrayList<>();
}