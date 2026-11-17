package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;

/**
 * Interface defining the tax rate behaviors.
 *
 * @author Sandra Jose (sajose)
 */
public interface TaxRateService {

    /**
     * Returns the current tax rate, creating it with the default rate if it
     * does not exist yet.
     * @return tax rate dto object
     */
    TaxRateDto getTaxRate();

    /**
     * Updates the tax rate.
     * @param taxRateDto contains the new rate as a percent
     * @return updated tax rate dto object
     */
    TaxRateDto updateTaxRate(TaxRateDto taxRateDto);
}
