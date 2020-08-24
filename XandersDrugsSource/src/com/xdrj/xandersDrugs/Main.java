package com.xdrj.xandersDrugs;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("deprecation")
public class Main extends JavaPlugin {

	private static JSONObject items;
	private static JSONObject customRecipes;
	private static JSONObject drugEffects;
	private static ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>();

	@Override
	public void onEnable() {

		// Runs on the enable of the plugin
		saveResources();
		items = getCustomItemsJSON();
		registerCraftingRecipes();
		customRecipes = getCustomRecipes();
		drugEffects = getDrugEffects();
		ArrayList<ItemStack> recipeItems = getCustomRecipeIngredients();

		// Registers event listeners
		getServer().getPluginManager().registerEvents(new CraftListener(this), this);
		getServer().getPluginManager().registerEvents(new DropListener(customRecipes, recipeItems), this);
		getServer().getPluginManager().registerEvents(new PlayerUseListener(drugEffects), this);
		getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
	}

	@Override
	public void onDisable() {
		getServer().resetRecipes();
	}

	private void saveResources() {
		// Saves all of the files within the jar to the special Resources folder
		// provided by Bukkit
		this.saveResource("crafting_recipes.json", false);
		this.saveResource("custom_items.json", false);
		this.saveResource("custom_recipes.json", false);
		this.saveResource("drug_effects.json", false);

		// Creates a new empty PlacedBlockData and saves it so that PlacedBlockData will
		// not read null or give an End Of File Exception, unless there is already a
		// PlacedBlockData stored

		PlacedBlockData data = new PlacedBlockData(new HashMap<Location, String>());
		if (PlacedBlockData.loadData("placed_blocks.data") == null) {
			data.saveData("placed_blocks.data");
		}
	}

	public static ItemStack getCustomItem(String itemToGet) {
		// Returns the ItemStack from the list of custom ItemStacks
		return (ItemStack) items.get(itemToGet);
	}

	@SuppressWarnings("unchecked")
	private JSONObject getCustomItemsJSON() {
		JSONObject stackList = new JSONObject();

		try {
			// Creates a new JSON Parser and gets the JSONObject from the InputStreamReader
			JSONParser parser = new JSONParser();
			InputStreamReader istream = new InputStreamReader(this.getResource("custom_items.json"), "UTF-8");
			Object obj = parser.parse(istream);
			JSONObject jo = (JSONObject) obj;

			// Gets the key 'items' from the JSONObject
			// which returns another JSONObject with the item information inside
			JSONObject items = (JSONObject) jo.get("items");

			// Creates an iterator over the set of keys of the items JSON
			Iterator<?> iterator = items.keySet().iterator();
			int i = 0;

			// While there is another key in the set, the loops
			while (iterator.hasNext()) {
				Object next = iterator.next();
				// Gets the JSONObject of the current key
				JSONObject item = (JSONObject) items.get(next);
				++i;
				// Gets the item information using hardcoded keys, prints for debug
				String customItemID = next.toString();
				String dispName = (String) item.get("displayName");
				System.out.println("Creating Item " + item.get("displayName").toString() + " From JSON (" + i + " of "
						+ items.keySet().size() + ")");
				String matString = (String) item.get("itemMaterial");
				Material mat = Material.getMaterial(matString);
				Boolean enchanted = (Boolean) item.get("isEnchanted");
				int potionColor = ((Long) item.get("potionColor")).intValue();

				// Creates the ItemStack for the item
				ItemStack is = new ItemStack(mat);
				// Gets MetaData from item to assign values to
				ItemMeta im = is.getItemMeta();

				if (enchanted) {
					// Adds hidden enchantment for effect
					im.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
					im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				}

				if (mat == Material.POTION) {
					// Casts ItemMeta to PotionMeta so item can have proper colors
					PotionMeta pm = (PotionMeta) im;

					// Gets hex string from potioncolor int and
					// turns it into RGB ints, then sets potion color
					String hexColor = Integer.toHexString(potionColor);
					int r = Integer.valueOf(hexColor.substring(0, 2), 16);
					int b = Integer.valueOf(hexColor.substring(2, 4), 16);
					int g = Integer.valueOf(hexColor.substring(4, 6), 16);
					pm.setColor(Color.fromRGB(r, b, g));

					// RESET is need so the name will not display with italics
					pm.setDisplayName(ChatColor.RESET + dispName);

					// Creates new NamespacedKey (NSK) that custom values will be
					// found under for all custom items
					NamespacedKey datakey = new NamespacedKey(this, "drugID");

					// Adds custom item ID to PersistentDataContainer under NSK
					pm.getPersistentDataContainer().set(datakey, PersistentDataType.STRING, customItemID);

					// Applies meta to itemstack
					is.setItemMeta(pm);
				} else {
					// Same function for non-potion items
					im.setDisplayName(ChatColor.RESET + dispName);
					NamespacedKey datakey = new NamespacedKey(this, "drugID");
					im.getPersistentDataContainer().set(datakey, PersistentDataType.STRING, customItemID);
					is.setItemMeta(im);
				}
				// Adds item to JSON list of itemstacks under its ID
				// as well as the arraylist of itemstacks
				stackList.put(customItemID, is);
				itemsList.add(is);
			}

			return stackList;
		} catch (Exception e) {
			e.printStackTrace();
			return stackList;
		}
	}

	private void registerCraftingRecipes() {
		try {
			// Gets JSONObject with recipes using parser
			// and creates iterator from keyset
			JSONParser parser = new JSONParser();
			InputStreamReader istream = new InputStreamReader(this.getResource("crafting_recipes.json"), "UTF-8");
			Object obj = parser.parse(istream);
			JSONObject jo = (JSONObject) obj;
			JSONObject recipes = (JSONObject) jo.get("recipes");
			Iterator<?> iterator = recipes.keySet().iterator();

			while (iterator.hasNext()) {
				// Gets JSONObject of next key
				Object next = iterator.next();
				JSONObject recipe = (JSONObject) recipes.get(next);

				// Creates new NamespacedKey (NSK) using the result customID
				// because each recipe needs a unique NSK
				NamespacedKey key = new NamespacedKey(this, next.toString());

				// Gets the ItemStack associated with the customID
				ItemStack result = getCustomItem(recipe.get("result").toString());

				// Print for debug
				System.out.println("Starting Recipe " + recipe.get("result").toString());

				// Checks whether the recipe is shapeless
				String isShapeless = recipe.get("shapeless").toString();
				if (Boolean.parseBoolean(isShapeless)) {

					// Checks amount and applies it to itemstack if over one
					int amount = ((Long) recipe.get("amount")).intValue();
					if (amount > 1) {
						result.setAmount(amount);
					}

					// Creates new Shapeless recipe object and gets ingredients from JSON
					ShapelessRecipe sr = new ShapelessRecipe(key, result);
					JSONArray ingredients = (JSONArray) recipe.get("ingredients");

					// Iterates for each ingredient
					for (int i = 0; i < ingredients.size(); ++i) {
						// Gets string from JSON
						String ingredient = ingredients.get(i).toString();
						// Gets material from string and adds it to recipe
						sr.addIngredient(Material.getMaterial(ingredient));
					}

					// Adds recipe to server and prints for debug
					Bukkit.addRecipe(sr);
					System.out.println("Registered Recipe " + recipe.get("result").toString());

					// Sets result itemstack to 1
					// NOTE: would be unneccessary if itemstack was cloned, which it should be,
					// but i was unaware when i wrote this code
					result.setAmount(1);
				} else {
					// Checks amount and applies it to itemstack if over one
					int amount = ((Long) recipe.get("amount")).intValue();
					if (amount > 1) {
						result.setAmount(amount);
					}

					// Creates new ShapeRecipe
					ShapedRecipe sr = new ShapedRecipe(key, result);

					// Gets pattern JSONArray from JSON, applies to ShapedRecipe
					JSONArray pattern = (JSONArray) recipe.get("pattern");
					sr.shape(pattern.get(0).toString(), pattern.get(1).toString(), pattern.get(2).toString());

					// Iterates for every ingredient
					JSONObject ingredients = (JSONObject) recipe.get("ingredients");
					Iterator<?> ingIterator = ingredients.keySet().iterator();
					while (ingIterator.hasNext()) {
						// Gets character associated with ingredient for pattern
						// and applies it and ItemStack from ID to ShapedRecipe
						// using ExactChoice, which takes an ItemStack
						// NOTE: deprecated due to draft, but is completely functional
						Object ingNext = ingIterator.next();
						char itemKey = ingNext.toString().charAt(0);
						String itemValue = (String) ingredients.get(ingNext.toString());
						ExactChoice ec = new ExactChoice(getCustomItem(itemValue));
						sr.setIngredient(itemKey, ec);
					}
					// Adds recipe to server and prints for debug
					Bukkit.addRecipe(sr);
					System.out.println("Registered Recipe " + recipe.get("result").toString());

					// See above note
					result.setAmount(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONObject getCustomRecipes() {
		try {
			// Returns JSONObject from file
			JSONParser parser = new JSONParser();
			InputStreamReader istream = new InputStreamReader(this.getResource("custom_recipes.json"), "UTF-8");
			Object obj = parser.parse(istream);
			JSONObject jo = (JSONObject) obj;
			JSONObject recipes = (JSONObject) jo.get("recipes");
			return recipes;
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}

	private JSONObject getDrugEffects() {
		try {
			// Returns JSONObject from file
			JSONParser parser = new JSONParser();
			InputStreamReader istream = new InputStreamReader(this.getResource("drug_effects.json"), "UTF-8");
			Object obj = parser.parse(istream);
			JSONObject jo = (JSONObject) obj;
			JSONObject effects = (JSONObject) jo.get("effects");
			return effects;
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}

	private ArrayList<ItemStack> getCustomRecipeIngredients() {
		// Returns ItemStack ArrayList of ingredients in recipes
		ArrayList<ItemStack> ingredients = new ArrayList<ItemStack>();
		Iterator<?> cri = customRecipes.keySet().iterator();
		while (cri.hasNext()) {
			String next = (String) cri.next();
			ItemStack ingredient = getCustomItem(((JSONObject) customRecipes.get(next)).get("ingredient").toString());
			ingredients.add(ingredient);
		}
		return ingredients;
	}
}
