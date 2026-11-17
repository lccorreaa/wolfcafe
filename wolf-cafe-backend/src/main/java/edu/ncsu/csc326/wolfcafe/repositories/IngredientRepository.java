package edu.ncsu.csc326.wolfcafe.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

/**
 * IngredientRepository for working with the DB through the
 * JpaRepository.
 *
 * @author CSC 326 Course Staff
 */
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * Finds an Ingredient with the given name.
     *
     * @param name ingredient's name
     * @return the found ingredient, empty if none
     */
    Optional<Ingredient> findByName(String name);

}
