package com.groupbuying.service.impl;

import com.groupbuying.dto.ProductDTO;
import com.groupbuying.entity.Product;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.ProductRepository;
import com.groupbuying.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product = productRepository.save(product);
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        BeanUtils.copyProperties(productDTO, product, "id", "createdAt");
        product = productRepository.save(product);
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    BeanUtils.copyProperties(product, dto);
                    return dto;
                });
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    BeanUtils.copyProperties(product, dto);
                    return dto;
                });
    }

    @Override
    public Page<ProductDTO> getProductsByCategoryLike(String category, Pageable pageable) {
        return productRepository.findByCategoryLike(category, pageable)
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    BeanUtils.copyProperties(product, dto);
                    return dto;
                });
    }

    @Override
    public Page<ProductDTO> searchProducts(String category, String keyword, Pageable pageable) {
        return productRepository.findByCategoryAndNameContainingIgnoreCase(category, keyword, pageable)
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    BeanUtils.copyProperties(product, dto);
                    return dto;
                });
    }

    @Override
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        if (product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        
        product.setStock(product.getStock() - quantity);
        product.setSales(product.getSales() + quantity);
        productRepository.save(product);
    }

    @Override
    public Page<ProductDTO> searchProductsByName(String keyword, Pageable pageable) {
        System.out.println("执行名称搜索，关键词: " + keyword);
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        System.out.println("找到的产品数量: " + products.getTotalElements());
        if (products.getTotalElements() > 0) {
            System.out.println("第一个产品名称: " + products.getContent().get(0).getName());
        }
        return products.map(product -> {
            ProductDTO dto = new ProductDTO();
            BeanUtils.copyProperties(product, dto);
            return dto;
        });
    }
} 