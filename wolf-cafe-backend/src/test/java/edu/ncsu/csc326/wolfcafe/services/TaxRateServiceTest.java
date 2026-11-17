package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repositories.TaxRateRepository;
import jakarta.persistence.EntityManager;

/**
 * Tests TaxRateServiceImpl.
 *
 * @author Sandra Jose (sajose)
 */
@SpringBootTest
@Transactional
public class TaxRateServiceTest {

    /** Reference to TaxRateService (and TaxRateServiceImpl) */
    @Autowired
    private TaxRateService taxRateService;

    /** Reference to TaxRateRepository, used to empty the table */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;

    /**
     * Empties the tax rate table before each test.
     */
    @BeforeEach
    public void setUp() {
        taxRateRepository.deleteAll();
        entityManager.flush();
    }

    /**
     * Builds a TaxRateDto with the given rate.
     * @param rate tax rate percent
     * @return the TaxRateDto
     */
    private TaxRateDto dto(Double rate) {
        TaxRateDto taxRateDto = new TaxRateDto();
        taxRateDto.setRate(rate);
        return taxRateDto;
    }

    /**
     * Tests that getting the tax rate when none exists creates the default.
     */
    @Test
    public void testGetTaxRateCreatesDefault() {
        TaxRateDto taxRateDto = taxRateService.getTaxRate();
        assertNotNull(taxRateDto.getId());
        assertEquals(TaxRate.DEFAULT_RATE, taxRateDto.getRate());
        assertEquals(1, taxRateRepository.count());

        // getting it again does not create a second row
        assertEquals(taxRateDto.getId(), taxRateService.getTaxRate().getId());
        assertEquals(1, taxRateRepository.count());
    }

    /**
     * Tests updating the tax rate with valid values, including the boundary of 100.
     */
    @Test
    public void testUpdateTaxRateValid() {
        Long id = taxRateService.getTaxRate().getId();

        TaxRateDto updated = taxRateService.updateTaxRate(dto(8.0));
        assertEquals(8.0, updated.getRate());
        assertEquals(id, updated.getId());
        assertEquals(8.0, taxRateService.getTaxRate().getRate());

        assertEquals(100.0, taxRateService.updateTaxRate(dto(100.0)).getRate());
        assertEquals(0.01, taxRateService.updateTaxRate(dto(0.01)).getRate());
        assertEquals(1, taxRateRepository.count());
    }

    /**
     * Tests that updating creates the tax rate row if it does not exist yet.
     */
    @Test
    public void testUpdateTaxRateWhenNoneExists() {
        TaxRateDto updated = taxRateService.updateTaxRate(dto(7.5));
        assertEquals(7.5, updated.getRate());
        assertEquals(1, taxRateRepository.count());
    }

    /**
     * Tests that invalid rates are rejected and the stored rate is unchanged.
     */
    @Test
    public void testUpdateTaxRateInvalid() {
        taxRateService.updateTaxRate(dto(5.0));

        for (Double bad : new Double[] { 0.0, -1.0, 100.01, null, Double.NaN }) {
            WolfCafeAPIException e = assertThrows(WolfCafeAPIException.class,
                    () -> taxRateService.updateTaxRate(dto(bad)));
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        }
        assertThrows(WolfCafeAPIException.class, () -> taxRateService.updateTaxRate(null));

        assertEquals(5.0, taxRateService.getTaxRate().getRate());
    }
}
