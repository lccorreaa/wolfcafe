package edu.ncsu.csc326.wolfcafe.controllers;

import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.services.TaxRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for tax rate. Every logged-in user can view the tax rate,
 * but only the admin can update it.
 *
 * @author Sandra Jose (sajose)
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/tax")
public class TaxRateController {

    /** tax rate service object */
    @Autowired
    private TaxRateService taxRateService;

    /** audit logger object */
    @Autowired
    private AuditLogger auditLogger;

    /**
     * REST API endpoint to get the current tax rate.
     * @return response containing the current tax rate
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @GetMapping
    public ResponseEntity<TaxRateDto> getTaxRate(){
        TaxRateDto taxRateDto = taxRateService.getTaxRate();
        return ResponseEntity.ok(taxRateDto);
    }

    /**
     * REST API endpoint to update the tax rate. Admin only.
     * @param taxRateDto contains the new rate as a percent
     * @return response containing the updated tax rate
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<TaxRateDto> updateTaxRate(@RequestBody TaxRateDto taxRateDto){
        TaxRateDto savedTaxRateDto = taxRateService.updateTaxRate(taxRateDto);
        auditLogger.log("updated tax rate to " + savedTaxRateDto.getRate() + "%");
        return ResponseEntity.ok(savedTaxRateDto);
    }
}
