package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;

/**
 * Converts between ItemDto and Item entity.
 *
 * @author CSC 326 Course Staff
 */
public final class ItemMapper {

	/**
	 * Utility class; not meant to be instantiated.
	 */
	private ItemMapper() {
	}

	/**
	 * Converts an Item entity to ItemDto.
	 * @param item Item to convert
	 * @return ItemDto object
	 */
	public static ItemDto mapToItemDto(Item item) {
		return new ItemDto(
				item.getId(),
				item.getName(),
				item.getDescription(),
				item.getPrice(),
				item.getAmount()
		);
	}

	/**
	 * Converts an ItemDto object to an Item entity.
	 * @param itemDto ItemDto to convert
	 * @return Item entity
	 */
	public static Item mapToItem(ItemDto itemDto) {
		return new Item(
				itemDto.getId(),
				itemDto.getName(),
				itemDto.getDescription(),
				itemDto.getPrice(),
				itemDto.getAmount()
		);
	}

}
