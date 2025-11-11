package com.swp.project.repository.product;
import com.swp.project.entity.product.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    boolean existsByName(String name);

    @Query("SELECT DISTINCT c FROM Product p JOIN p.categories c WHERE p.id IN :productIds")
    List<Category> findDistinctCategoriesByProductIds(@Param("productIds") List<Long> productIds);

    @Query("""
        SELECT c 
        FROM Category c
        WHERE LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :categoryName, '%')))
""")
    Page<Category> findByCategoryName(String categoryName, Pageable pageable);

    Page<Category> findAll(Pageable pageable);

    List<Category> findByIsActiveTrue(Sort by);
}
