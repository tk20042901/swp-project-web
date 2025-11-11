package com.swp.project.repository.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    Product findByName(String productName);

    List<Product> getByName(String name);

    List<Product> findDistinctByCategoriesInAndIdNot(List<Category> categories, Long id, Pageable pageable);

    @Query("""
        SELECT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p JOIN p.categories c 
        WHERE c.id = :categoryId AND p.enabled = :enabled
    """)
    Page<ViewProductDto> findViewProductDtoByCategoryIdAndEnabled(
        @Param("categoryId") Long categoryId,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query("""
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p JOIN p.categories c 
        WHERE c.id = :categoryId AND p.enabled = :enabled
    """)
    List<ViewProductDto> findViewProductDtoByCategoryIdAndEnabled(
        @Param("categoryId") Long categoryId,
        @Param("enabled") boolean enabled
    );

    @Query("""
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        WHERE p.enabled = :enabled
    """)
    Page<ViewProductDto> findAllViewProductDtoByEnabled(
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    @Query("""
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        WHERE p.enabled = :enabled
    """)
    List<ViewProductDto> findAllViewProductDtoByEnabled(
        @Param("enabled") boolean enabled
    );

    @Query(""" 
        SELECT NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        JOIN p.categories c 
        WHERE p.enabled = :enabled
        AND c.id = :categoryId
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    Page<ViewProductDto> findViewProductDtoByProductNameAndCategoryIdAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    @Query(""" 
        SELECT  NEW com.swp.project.dto.ViewProductDto(p.id, p.name, p.price, p.main_image_url, p.soldQuantity) 
        FROM Product p 
        WHERE p.enabled = :enabled
        AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%')))
    """)
    Page<ViewProductDto> findViewProductDtoByProductNameAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );

    List<Product> findAllByEnabled(boolean enabled);

    /**
     * Optimized query to get homepage products in batches
     * This method returns different product sets based on sort criteria to reduce database calls
     */
    @Query(value = """
        (SELECT p.id, p.name, p.price, p.main_image_url, p.sold_quantity, 'newest' as sort_type
         FROM products p 
         WHERE p.enabled = true 
         ORDER BY p.id DESC 
         LIMIT :limit)
        UNION ALL
        (SELECT p.id, p.name, p.price, p.main_image_url, p.sold_quantity, 'best-seller' as sort_type
         FROM products p 
         WHERE p.enabled = true 
         ORDER BY p.sold_quantity DESC 
         LIMIT :limit)
        UNION ALL
        (SELECT p.id, p.name, p.price, p.main_image_url, p.sold_quantity, 'default' as sort_type
         FROM products p 
         WHERE p.enabled = true 
         ORDER BY p.id 
         LIMIT :limit)
        """, nativeQuery = true)
    List<Object[]> findHomepageProductsBatch(@Param("limit") int limit);

    @Query("""
    SELECT p
    FROM Product p
    WHERE (:enabled IS NULL OR p.enabled = :enabled)
      AND LOWER(FUNCTION('unaccent', p.name )) 
          LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :name, '%')))
    """)
    Page<Product> findByNameContainingIgnoreCaseAndEnabled(String name, Boolean enabled, Pageable pageable);

    @Query("""
    SELECT p
    FROM Product p
    WHERE (:enabled IS NULL OR p.enabled = :enabled)
      AND LOWER(FUNCTION('unaccent', p.name )) 
          LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :name, '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<Product> findByNameContainingIgnoreCaseAndEnabledAndPrice(String name, Boolean enabled,Long minPrice, Long maxPrice, Pageable pageable);
    @Query("""
    SELECT p 
    FROM Product p
    WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :name, '%')))
""")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByEnabled(Boolean enabled, Pageable pageable);

    Product findFirstByEnabledOrderByIdAsc(boolean enabled);

    Product findTopByOrderByIdDesc();

    @Query(value = """
        SELECT 
            p.id AS productId,
            p.name AS productName,
            pu.name AS productUnit,
            p.main_image_url AS mainImageUrl,
            COALESCE(SUM(
                CASE 
                    WHEN o.payment_method_id = 'COD' AND os.name = 'Đã Giao Hàng' 
                        THEN oi.quantity
                    WHEN o.payment_method_id = 'QR' AND os.name IN ('Đã Giao Hàng', 'Đang Giao Hàng', 'Đang Chuẩn Bị Hàng') 
                        THEN oi.quantity
                    ELSE 0
                END
            ), 0) AS totalSold,
            COALESCE(SUM(
                CASE 
                    WHEN o.payment_method_id = 'COD' AND os.name = 'Đã Giao Hàng' 
                        THEN oi.quantity * oi.price
                    WHEN o.payment_method_id = 'QR' AND os.name IN ('Đã Giao Hàng', 'Đang Giao Hàng', 'Đang Chuẩn Bị Hàng') 
                        THEN oi.quantity * oi.price
                    ELSE 0
                END
            ), 0) AS revenue
        FROM product p
        LEFT JOIN order_item oi ON oi.product_id = p.id
        LEFT JOIN orders o ON oi.order_id = o.id
        LEFT JOIN order_status os ON o.order_status_id = os.id
                LEFT JOIN product_unit pu ON pu.id = p.unit_id
          GROUP BY p.id, p.name,pu.name ,p.main_image_url
                ORDER BY revenue DESC
        """, nativeQuery = true)
    Page<Object[]> getProductSalesAndRevenue(Pageable pageable);

    @Query(
            value = """
  SELECT 
    p.id, p.name,pu.name ,p.main_image_url,
    COALESCE(SUM(CASE
      WHEN o.payment_method_id = 'COD' AND os.name = 'Đã Giao Hàng' THEN oi.quantity
      WHEN o.payment_method_id = 'QR' AND os.name IN ('Đã Giao Hàng','Đang Giao Hàng','Đang Chuẩn Bị Hàng') THEN oi.quantity
      ELSE 0 END),0) AS totalSold,
    COALESCE(SUM(CASE
      WHEN o.payment_method_id = 'COD' AND os.name = 'Đã Giao Hàng' THEN oi.quantity * oi.price
      WHEN o.payment_method_id = 'QR' AND os.name IN ('Đã Giao Hàng','Đang Giao Hàng','Đang Chuẩn Bị Hàng') THEN oi.quantity * oi.price
      ELSE 0 END),0) AS revenue
  FROM product p
  LEFT JOIN order_item oi ON oi.product_id = p.id
  LEFT JOIN orders o ON oi.order_id = o.id
  LEFT JOIN order_status os ON o.order_status_id = os.id
        LEFT JOIN product_unit pu ON pu.id = p.unit_id
  WHERE (:keyWord IS NULL OR unaccent(lower(p.name)) LIKE unaccent(lower(CONCAT('%', :keyWord, '%'))))
  GROUP BY p.id, p.name,pu.name ,p.main_image_url
  ORDER BY revenue DESC
  """, nativeQuery = true
    )
    Page<Object[]> searchProductSalesAndRevenue(@Param("keyWord") String keyWord, Pageable pageable);


    @Query(value = """
        SELECT 
            p.id AS productId,
            p.name AS productName,
            pu.name AS productUnit,
            p.main_image_url AS mainImageUrl,
            COALESCE(SUM(
                CASE 
                    WHEN o.payment_method_id = 'COD' AND os.name = 'Đã Giao Hàng' 
                        THEN oi.quantity
                    WHEN o.payment_method_id = 'QR' AND os.name IN ('Đã Giao Hàng', 'Đang Giao Hàng', 'Đang Chuẩn Bị Hàng') 
                        THEN oi.quantity
                    ELSE 0
                END
            ), 0) AS totalSold,
            COALESCE(SUM(
                CASE 
                    WHEN o.payment_method_id = 'COD' AND os.name = 'Đã Giao Hàng' 
                        THEN oi.quantity * oi.price
                    WHEN o.payment_method_id = 'QR' AND os.name IN ('Đã Giao Hàng', 'Đang Giao Hàng', 'Đang Chuẩn Bị Hàng') 
                        THEN oi.quantity * oi.price
                    ELSE 0
                END
            ), 0) AS revenue
        FROM product p
        LEFT JOIN order_item oi ON oi.product_id = p.id
        LEFT JOIN orders o ON oi.order_id = o.id
        LEFT JOIN order_status os ON o.order_status_id = os.id
                LEFT JOIN product_unit pu ON pu.id = p.unit_id
        GROUP BY p.id, p.name,pu.name ,p.main_image_url
        ORDER BY revenue DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getTop5ProductRevenue();
}
