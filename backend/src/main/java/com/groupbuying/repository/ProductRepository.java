package com.groupbuying.repository;

import com.groupbuying.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.category = :category")
    Page<Product> findByCategory(@Param("category") String category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.name LIKE %:name%")
    Page<Product> findByCategoryAndNameContainingIgnoreCase(@Param("category") String category, @Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category LIKE %:category%")
    Page<Product> findByCategoryLike(@Param("category") String category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
} 