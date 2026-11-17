package edu.ncsu.csc326.wolfcafe.repositories;

import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * TaxRateRepository for working with the DB through the
 * JpaRepository.
 *
 * @author Sandra Jose (sajose)
 */
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

}
