package com.groupbuying.service;

import com.groupbuying.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
    ProductDTO getProduct(Long id);
    Page<ProductDTO> getAllProducts(Pageable pageable);
    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);
    Page<ProductDTO> getProductsByCategoryLike(String category, Pageable pageable);
    Page<ProductDTO> searchProducts(String category, String keyword, Pageable pageable);
    Page<ProductDTO> searchProductsByName(String keyword, Pageable pageable);
    void updateStock(Long id, Integer quantity);
} 