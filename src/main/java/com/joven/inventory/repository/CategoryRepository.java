package com.joven.inventory.repository;

import com.joven.inventory.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Category} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its name.
     *
     * @param name the category name to search for
     * @return an Optional containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Checks if a category exists with the given name.
     *
     * @param name the category name to check
     * @return true if a category with the name exists
     */
    boolean existsByName(String name);

    /**
     * Finds all active categories with pagination.
     *
     * @param pageable pagination information
     * @return a page of active categories
     */
    Page<Category> findByActiveTrue(Pageable pageable);

    /**
     * Finds all active categories ordered by name ascending.
     *
     * @return a list of active categories sorted by name
     */
    List<Category> findByActiveTrueOrderByNameAsc();
}
