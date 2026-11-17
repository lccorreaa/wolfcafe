package edu.ncsu.csc326.wolfcafe.services.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.mapper.ItemMapper;
import edu.ncsu.csc326.wolfcafe.repositories.ItemRepository;
import edu.ncsu.csc326.wolfcafe.services.ItemService;
import lombok.AllArgsConstructor;

/**
 * Implemented item service
 *
 * @author CSC 326 Course Staff
 */
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

	/** Item repository */
	private ItemRepository itemRepository;

	/**
	 * Adds given item
	 * @param itemDto item to add
	 * @return added item
	 * @throws WolfCafeAPIException with status CONFLICT if an item already has that name
	 */
	@Override
	public ItemDto addItem(ItemDto itemDto) {
		checkNameAvailable(itemDto.getName(), null);
		Item item = ItemMapper.mapToItem(itemDto);
		//Always let the database assign the id of a new item
		item.setId(null);
		Item savedItem = itemRepository.save(item);
		return ItemMapper.mapToItemDto(savedItem);
	}

	/**
	 * Gets item by id
	 * @param id id of item to get
	 * @return returned item
	 * @throws ResourceNotFoundException if the item doesn't exist
	 */
	@Override
	public ItemDto getItem(Long id) {
		Item item = itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
		return ItemMapper.mapToItemDto(item);
	}

	/**
	 * Returns all items
	 * @return all items
	 */
	@Override
	public List<ItemDto> getAllItems() {
		List<Item> items = itemRepository.findAll();
		return items.stream().map(ItemMapper::mapToItemDto).toList();
	}

	/**
	 * Updates the item with the given id
	 * @param id id of item to update
	 * @param itemDto information of item to update
	 * @return updated item
	 * @throws ResourceNotFoundException if the item doesn't exist
	 * @throws WolfCafeAPIException with status CONFLICT if another item already has the new name
	 */
	@Override
	public ItemDto updateItem(Long id, ItemDto itemDto) {
		Item item = itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
		checkNameAvailable(itemDto.getName(), id);
		item.setName(itemDto.getName());
		item.setDescription(itemDto.getDescription());
		item.setPrice(itemDto.getPrice());
		Item updatedItem = itemRepository.save(item);
		return ItemMapper.mapToItemDto(updatedItem);
	}

	/**
	 * Deletes the item with the given id
	 * @param id id of item to delete
	 * @throws ResourceNotFoundException if the item doesn't exist
	 */
	@Override
	public void deleteItem(Long id) {
		itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
		itemRepository.deleteById(id);
	}

	/**
	 * Checks that no item other than the one with the given id uses the name.
	 * Item names are unique in the database, so without this check a duplicate
	 * name would fail inside the database and reach the client as a 500 error.
	 * @param name name to check
	 * @param id id of the item being updated, or null for a new item
	 * @throws WolfCafeAPIException with status CONFLICT if the name is taken
	 */
	private void checkNameAvailable(String name, Long id) {
		itemRepository.findByName(name).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new WolfCafeAPIException(HttpStatus.CONFLICT, "Item already exists with name " + name);
			}
		});
	}
}
