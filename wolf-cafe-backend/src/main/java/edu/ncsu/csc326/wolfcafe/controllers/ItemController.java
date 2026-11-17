package edu.ncsu.csc326.wolfcafe.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.services.ItemService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Controller for API endpoints for an Item.
 *
 * Each endpoint states the roles allowed to call it with @PreAuthorize.  A request
 * with no valid login token is answered with 401 Unauthorized, and a logged in
 * user without one of the listed roles is answered with 403 Forbidden.
 *
 * TODO - update roles as appropriate for your team's requirements
 *
 * @author CSC 326 Course Staff
 */
@RestController
@RequestMapping("api/items")
@AllArgsConstructor
@CrossOrigin("*")
public class ItemController {

	/** Link to ItemService */
	private ItemService itemService;

	/** Records who called each endpoint */
	private AuditLogger auditLogger;

	/**
	 * Adds an item to the list of items.  Requires the STAFF or ADMIN role.
	 * @param itemDto item to add
	 * @return added item
	 */
	@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
	@PostMapping
	public ResponseEntity<ItemDto> addItem(@Valid @RequestBody ItemDto itemDto) {
		ItemDto savedItem = itemService.addItem(itemDto);
		auditLogger.log("created item " + savedItem.getId() + " (" + savedItem.getName()
				+ ") Price: " + savedItem.getPrice() + " Amount: " + savedItem.getAmount());
		return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
	}

	/**
	 * Gets an item by id.  Requires the ADMIN, STAFF or CUSTOMER role.
	 * @param id item id
	 * @return item with the id
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
	@GetMapping("{id}")
	public ResponseEntity<ItemDto> getItem(@PathVariable("id") Long id) {
		ItemDto item = itemService.getItem(id);
		auditLogger.log("viewed item " + id);
		return ResponseEntity.ok(item);
	}

	/**
	 * Returns all items.  Requires the ADMIN, STAFF or CUSTOMER role.
	 * @return a list of all items
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
	@GetMapping
	public ResponseEntity<List<ItemDto>> getAllItems() {
		List<ItemDto> items = itemService.getAllItems();
		auditLogger.log("viewed all items");
		return ResponseEntity.ok(items);
	}

	/**
	 * Updates the item with the given id.  Requires the ADMIN role.
	 * @param id item to update
	 * @param itemDto information about the item to update
	 * @return updated item
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("{id}")
	public ResponseEntity<ItemDto> updateItem(@PathVariable("id") Long id, @Valid @RequestBody ItemDto itemDto) {
		ItemDto updatedItem = itemService.updateItem(id, itemDto);
		auditLogger.log("updated item " + id + " (" + updatedItem.getName() + ")");
		return ResponseEntity.ok(updatedItem);
	}

	/**
	 * Deletes the item with the given id.  Requires the ADMIN role.
	 * @param id item to delete
	 * @return response indicating success or failure
	 */
	@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
	@DeleteMapping("{id}")
	public ResponseEntity<String> deleteItem(@PathVariable("id") Long id) {
		itemService.deleteItem(id);
		auditLogger.log("deleted item " + id);
		return ResponseEntity.ok("Item deleted successfully");
	}
}
