package edu.ncsu.csc326.wolfcafe.controllers;

import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for CoffeeMaker's inventory.  The inventory is a singleton; there's
 * only one row in the database that contains the current inventory for the system.
 *
 * @author CSC 326 Course Staff
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
	
	/**
	 * Connection to inventory service for manipulating the Inventory model.
	 */
	@Autowired
	private InventoryService inventoryService;

	/**
	 * logs the inventory operations as they occur
	 */
	@Autowired
	private AuditLogger auditLogger;
	
	 /**
     * REST API endpoint to provide GET access to the CoffeeMaker's singleton
     * Inventory. 
     *
     * @return response to the request
     */
	@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
	@GetMapping
	public ResponseEntity<InventoryDto> getInventory() {
		InventoryDto inventoryDto = inventoryService.getItemInventory();
		auditLogger.log("viewed item inventory");
		return ResponseEntity.ok(inventoryDto);
	}
	
	/**
	 * REST API endpoint to provide update access to the CoffeeMaker's singleton
	 * Inventory.
     * @param inventoryDto
     *            amounts to add to the current item quantities
     * @return response to the request
	 */
	@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
	@PutMapping
	public ResponseEntity<InventoryDto> updateInventory(@RequestBody InventoryDto inventoryDto) {
		InventoryDto savedInventoryDto = inventoryService.updateItemInventory(inventoryDto);
		//the service is validated and saved the request (AI-generated)
		Set<Long> updatedIds = inventoryDto.getItems().stream().map(ItemDto::getId).collect(Collectors.toSet());
		for (ItemDto item : savedInventoryDto.getItems()) {
			if (updatedIds.contains(item.getId())) {
				auditLogger.log("updated item " + item.getName() + " to quantity " + item.getAmount());
			}
		}
		return ResponseEntity.ok(savedInventoryDto);
	}

}
