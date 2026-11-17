package edu.ncsu.csc326.wolfcafe.services.impl;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.mapper.TaxRateMapper;
import edu.ncsu.csc326.wolfcafe.repositories.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.services.TaxRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service Implementation Class for Tax Rate. The tax rate is treated as a
 * singleton: there is only ever one row in the table.
 *
 * @author Sandra Jose (sajose)
 */
@Service
public class TaxRateServiceImpl implements TaxRateService {

    /** maximum allowed tax rate percent */
    private static final double MAX_RATE = 100.0;

    /** tax rate repository */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /**
     * Returns the single TaxRate row, if one has been created. Located by
     * reading the only row rather than by a hard-coded id.
     * @return the single TaxRate, or an empty Optional if none exists yet
     */
    private Optional<TaxRate> findSingleTaxRate(){
        return taxRateRepository.findAll().stream().findFirst();
    }

    /**
     * Returns the single TaxRate row, creating it with the default rate if it
     * does not exist yet.
     * @return the single TaxRate
     */
    private TaxRate getOrCreateTaxRate(){
        return findSingleTaxRate().orElseGet(() -> taxRateRepository.save(new TaxRate()));
    }

    /**
     * Returns the current tax rate, creating it with the default rate if it
     * does not exist yet.
     * @return tax rate dto object
     */
    @Override
    public TaxRateDto getTaxRate(){
        return TaxRateMapper.mapToTaxRateDto(getOrCreateTaxRate());
    }

    /**
     * Updates the tax rate. The rate must be greater than 0 and at most 100.
     * @param taxRateDto contains the new rate as a percent
     * @return updated tax rate dto object
     * @throws WolfCafeAPIException with BAD_REQUEST if the rate is missing or out of range
     */
    @Override
    public TaxRateDto updateTaxRate(TaxRateDto taxRateDto){
        Double rate = taxRateDto == null ? null : taxRateDto.getRate();
        if (rate == null || rate.isNaN() || rate <= 0 || rate > MAX_RATE) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
                    "Tax rate must be a positive number no greater than 100.");
        }

        TaxRate taxRate = getOrCreateTaxRate();
        taxRate.setRate(rate);
        TaxRate savedTaxRate = taxRateRepository.save(taxRate);
        return TaxRateMapper.mapToTaxRateDto(savedTaxRate);
    }
}
