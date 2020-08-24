package com.xdrj.xandersDrugs;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CraftListener implements Listener {

	private Main plugin;

	public CraftListener(Main plugin) {
		this.plugin = plugin;
	}

	// Runs on items being put into a crafting table
	@EventHandler
	public void onCraft(PrepareItemCraftEvent e) {
		try {
			if (e.getRecipe().getResult() != null) {
				ItemStack result = e.getRecipe().getResult();
				NamespacedKey nskey = new NamespacedKey(plugin, "drugID");
				try {
					// If result isn't a custom item
					if (result.getItemMeta().getPersistentDataContainer().get(nskey,
							PersistentDataType.STRING) == null) {

						// Iterates through each crafting slot
						for (int a = 1; a < 9; a++) {
							if (e.getInventory().getItem(a) != null) {

								// If item in slot is custom
								if (e.getInventory().getItem(a).getItemMeta().getPersistentDataContainer().getKeys()
										.contains(nskey)) {
									System.out.println("voided result");
									e.getInventory().setResult(new ItemStack(Material.AIR));
								}
							}
						}
					}

					// If exception thrown (if a normal item)
				} catch (Exception exc) {

					// See above
					for (int a = 1; a < 9; a++) {
						if (e.getInventory().getItem(a) != null) {
							if (e.getInventory().getItem(a).getItemMeta().getPersistentDataContainer().getKeys()
									.contains(nskey)) {
								System.out.println("voided result");
								e.getInventory().setResult(new ItemStack(Material.AIR));
							}
						}
					}
				}
			}
		} catch (Exception ex) {

		}
	}
}
