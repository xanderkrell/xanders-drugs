package com.xdrj.xandersDrugs;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

public class DropListener implements Listener {

	private JSONObject customRecipes;
	private ArrayList<ItemStack> ingredients = new ArrayList<ItemStack>();

	public DropListener(JSONObject customRecipes, ArrayList<ItemStack> ingredients) {
		this.customRecipes = customRecipes;
		this.ingredients = ingredients;
	}

	// Runs when a player drops an item
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		// Gets gets customID of item drop
		Item itemDrop = e.getItemDrop();
		String dropID = itemDrop.getItemStack().getItemMeta().getPersistentDataContainer()
				.get(new NamespacedKey(Main.getPlugin(Main.class), "drugID"), PersistentDataType.STRING);

		Boolean x = false;
		// Iterates for every ingredient in custom recipes
		for (ItemStack ing : ingredients) {
			// Gets customID of ingredient
			String itemID = ing.getItemMeta().getPersistentDataContainer()
					.get(new NamespacedKey(Main.getPlugin(Main.class), "drugID"), PersistentDataType.STRING);

			// Checks if dropped item is an ingredient
			if (itemID.equals(dropID)) {
				x = true;
			}
		}
		if (x) {
			// Checks blocks around item
			checkBlocks(itemDrop, dropID);
		}
	}

	private void checkBlocks(Item itemDrop, String itemID) {
		// Schedules a task for 1 second
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
			public void run() {
				// Gets block location and block object of item location
				String path = ("placed_blocks.data");
				PlacedBlockData data = new PlacedBlockData(PlacedBlockData.loadData(path));
				Location loc = itemDrop.getLocation();
				Block itemBlock = loc.getBlock();
				Location blockLoc = itemBlock.getLocation();

				// Check if block is custom
				if (data.placedBlocks.containsKey(blockLoc)) {
					if (data.placedBlocks.get(blockLoc).equals("custom_block") && customRecipes.containsKey(itemID)) {
						// Get block under item and checks type
						loc.add(new Vector(0, -1, 0));
						Block underBlock = loc.getBlock();
						if (underBlock.getType().equals(Material.AIR)) {
							// Get recipe and byproduct ItemStacks
							ItemStack reciperesult = Main
									.getCustomItem((String) (((JSONObject) customRecipes.get(itemID)).get("result")));
							ItemStack byproduct = null;
							if (!((String) (((JSONObject) customRecipes.get(itemID)).get("byproduct")) == null)) {
								byproduct = Main.getCustomItem(
										(String) (((JSONObject) customRecipes.get(itemID)).get("byproduct")));
							}
							// Spawns new items
							spawnNewItems(itemDrop, reciperesult, byproduct, 100L);
						}
					}
					// Runs unless item despawns
				} else if (!itemDrop.isDead()) {
					checkBlocks(itemDrop, itemID);
				}
			}
		}, 20L);
	}

	private void spawnNewItems(Item item, ItemStack reciperesult, ItemStack byproduct, Long delay) {
		// Deletes original item and sets amount to amount of ingredient
		item.remove();
		reciperesult.setAmount(item.getItemStack().getAmount());

		// Schedules item spawn with delay from JSON
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
			public void run() {
				item.getWorld().dropItem(item.getLocation(), reciperesult);
				if (byproduct != null) {
					item.getWorld().dropItem(item.getLocation().clone().subtract(new Vector(0, -0.5, 0)), byproduct);
				}
			}
		}, delay);
	}
}
