package com.joven.inventory.repository;

import com.joven.inventory.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Role} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its unique name.
     *
     * @param name the role name
     * @return an Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Finds all active roles.
     *
     * @return list of active roles
     */
    List<Role> findByActiveTrue();

    /**
     * Checks whether a role with the given name already exists.
     *
     * @param name the role name
     * @return true if a role with the name exists
     */
    boolean existsByName(String name);
}
