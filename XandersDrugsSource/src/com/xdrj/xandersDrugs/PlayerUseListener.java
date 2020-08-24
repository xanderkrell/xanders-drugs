package com.xdrj.xandersDrugs;

import java.util.Iterator;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;

public class PlayerUseListener implements Listener {

	private JSONObject drugEffects;

	public PlayerUseListener(JSONObject drugEffects) {
		this.drugEffects = drugEffects;
	}

	// Runs when player uses an item
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent e) {
		// If hand isn't empty
		if (e.hasItem()) {
			// Get item ID
			ItemStack item = e.getItem();
			if (item.getItemMeta().getPersistentDataContainer()
					.has(new NamespacedKey(Main.getPlugin(Main.class), "drugID"), PersistentDataType.STRING)) {
				String itemID = item.getItemMeta().getPersistentDataContainer()
						.get(new NamespacedKey(Main.getPlugin(Main.class), "drugID"), PersistentDataType.STRING);
				// If item is one that gives effects
				if (drugEffects.keySet().contains(itemID)) {
					// Iterates over effects given by item
					JSONObject effectList = (JSONObject) ((JSONObject) drugEffects.get(itemID)).get("effects");
					Iterator<?> iterator = effectList.keySet().iterator();
					while (iterator.hasNext()) {
						// Gets duration and amplification
						String next = (String) iterator.next();
						Long dur = (Long) ((JSONObject) effectList.get(next)).get("duration");
						Long amp = (Long) ((JSONObject) effectList.get(next)).get("amplifier");
						int duration;
						if (dur == 0) {
							duration = (int) 1;
						} else {
							// Sets duration to minutes (minute = num minutes * 20 ticks * 60 seconds)
							duration = (int) (dur * 20 * 60);
						}
						// Creates effect and applies to player
						PotionEffect effect = new PotionEffect(PotionEffectType.getByName(next), duration,
								(int) (amp - 1));
						effect.apply(e.getPlayer());
					}
					if (item.getAmount() == 1) {
						// Removes item from hand and cancels event (if not cancelled, server crash)
						e.setCancelled(true);
						e.getPlayer().getInventory().setItem(e.getHand(), null);
					} else if (item != null) {
						// Removes one item from stack and cancels event
						e.setCancelled(true);
						item.setAmount(item.getAmount() - 1);
					}
				} else {
					if (!e.isBlockInHand()) {
						// Cancels if no effect and not a block
						e.setCancelled(true);
					}
				}
			}
		}
	}
}
