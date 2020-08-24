package com.xdrj.xandersDrugs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {

	// Runs when a block is broken
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		try {
			// Gets PlacedBlockData from file
			String path = ("placed_blocks.data");
			PlacedBlockData data = new PlacedBlockData(PlacedBlockData.loadData(path));

			// Gets location of broken block
			Location loc = e.getBlock().getLocation();

			// Checks if the location is a custom block
			if (data.placedBlocks.containsKey(loc)) {
				// Gets custom item associated with block
				ItemStack itemToDrop = (ItemStack) Main.getCustomItem((String) data.placedBlocks.get(loc));
				itemToDrop.getItemMeta().setDisplayName(ChatColor.RESET + itemToDrop.getItemMeta().getDisplayName());

				// Cancels normal block drop
				e.setDropItems(false);

				// Drops custom item
				loc.getWorld().dropItemNaturally(loc, itemToDrop);

				// Removes location from PlacedBlocks and saves to file
				data.placedBlocks.remove(loc);
				data.saveData(path);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
