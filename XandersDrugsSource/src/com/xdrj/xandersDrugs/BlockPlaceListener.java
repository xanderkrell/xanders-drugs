package com.xdrj.xandersDrugs;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

	private Main plugin;

	public BlockPlaceListener(Main plugin) {
		this.plugin = plugin;
	}

	// Runs when a block is placed
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		NamespacedKey nskey = new NamespacedKey(plugin, "drugID");
		try {
			// Checks if block in hand is a custom block
			String itemID = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(nskey,
					PersistentDataType.STRING);

			if (itemID != null) {
				// Gets location of custom block
				Location loc = e.getBlock().getLocation();
				String path = "placed_blocks.data";
				PlacedBlockData data = new PlacedBlockData(PlacedBlockData.loadData(path));

				if (data.placedBlocks == null) {
					// Adds location and customID to new hashmap and stores in file
					HashMap<Location, String> fullHash = new HashMap<Location, String>();
					fullHash.put(loc, itemID);
					data = new PlacedBlockData(fullHash);
					data.saveData(path);

				} else {
					// Adds location and customID to hashmap and stores in file
					data.placedBlocks.put(loc, itemID);
					data.saveData(path);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
