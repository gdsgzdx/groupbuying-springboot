package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.ProductDTO;
import com.groupbuying.service.ProductService;
import com.groupbuying.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    
    private static final Map<String, String> categoryMap = new HashMap<>();
    static {
        categoryMap.put("家用电器", "家用电器");
        categoryMap.put("手机通讯数码", "手机通讯数码");
        categoryMap.put("电脑办公文具用品", "电脑办公文具用品");
        categoryMap.put("家具家居厨房", "家具家居厨房");
        categoryMap.put("男装女装童装内衣", "男装女装童装内衣");
        categoryMap.put("美妆个护清洁", "美妆个护清洁");
        categoryMap.put("女鞋箱包珠宝", "女鞋箱包珠宝");
        categoryMap.put("男鞋运动户外", "男鞋运动户外");
        categoryMap.put("食品酒类生鲜", "食品酒类生鲜");
        categoryMap.put("艺术礼品花园艺", "艺术礼品花园艺");
        categoryMap.put("图书文娱教育", "图书文娱教育");
        categoryMap.put("机票酒店旅游", "机票酒店旅游");
        categoryMap.put("支付保险金融", "支付保险金融");
        categoryMap.put("玩具乐器", "玩具乐器");
    }

    @PostMapping
    public Result<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        return Result.success(productService.createProduct(productDTO));
    }

    @PutMapping("/{id}")
    public Result<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return Result.success(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ProductDTO> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProduct(id));
    }

    @GetMapping
    public Result<Page<ProductDTO>> getAllProducts(Pageable pageable) {
        return Result.success(productService.getAllProducts(pageable));
    }

    @GetMapping("/category/{category}")
    public Result<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String category,
            Pageable pageable) {
        try {
            System.out.println("Received category: " + category);
            // 对 URL 编码的 category 进行解码
            String decodedCategory = java.net.URLDecoder.decode(category, "UTF-8");
            System.out.println("Decoded category: " + decodedCategory);
            // 获取对应的分类名称
            String mappedCategory = categoryMap.getOrDefault(decodedCategory, decodedCategory);
            System.out.println("Mapped category: " + mappedCategory);
            
            // 打印所有可用的分类
            System.out.println("Available categories in map: " + categoryMap);
            
            // 检查分类是否存在
            if (!categoryMap.containsValue(mappedCategory)) {
                System.err.println("Category not found: " + mappedCategory);
                return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "分类不存在: " + mappedCategory);
            }
            
            // 使用新的 findByCategoryLike 方法
            Page<ProductDTO> products = productService.getProductsByCategoryLike(mappedCategory, pageable);
            System.out.println("Found products: " + products.getTotalElements());
            
            if (products.getTotalElements() == 0) {
                System.out.println("No products found for category: " + mappedCategory);
            }
            
            return Result.success(products);
        } catch (Exception e) {
            System.err.println("Error in getProductsByCategory: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "获取分类产品失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam String keyword,
            Pageable pageable) {
        System.out.println("搜索关键词: " + keyword);
        System.out.println("搜索分类: " + category);
        
        if (category != null && !category.isEmpty()) {
            Page<ProductDTO> result = productService.searchProducts(category, keyword, pageable);
            System.out.println("分类搜索结果数量: " + result.getTotalElements());
            return Result.success(result);
        } else {
            Page<ProductDTO> result = productService.searchProductsByName(keyword, pageable);
            System.out.println("关键词搜索结果数量: " + result.getTotalElements());
            return Result.success(result);
        }
    }
} 