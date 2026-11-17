package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;

/**
 * Mapper class for Tax Rate
 *
 * @author Sandra Jose (sajose)
 */
public class TaxRateMapper {

    /**
     * Converts a TaxRate entity to a TaxRateDto
     * @param taxRate TaxRate to convert
     * @return TaxRateDto object
     */
    public static TaxRateDto mapToTaxRateDto(TaxRate taxRate){
        TaxRateDto taxRateDto = new TaxRateDto();
        taxRateDto.setId(taxRate.getId());
        taxRateDto.setRate(taxRate.getRate());
        return taxRateDto;
    }

    /**
     * Converts a TaxRateDto to a TaxRate entity
     * @param taxRateDto TaxRateDto to convert
     * @return TaxRate entity
     */
    public static TaxRate mapToTaxRate(TaxRateDto taxRateDto){
        return new TaxRate(taxRateDto.getId(), taxRateDto.getRate());
    }
}
