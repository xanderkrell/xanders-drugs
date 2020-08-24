package com.xdrj.xandersDrugs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Location;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class PlacedBlockData implements Serializable {

	// Required by Serializable
	private static transient final long serialVersionUID = 4855440801866464945L;

	public final HashMap<Location, String> placedBlocks;

	// Two ways of creating placedblockdata:
	// From hashmap and from loaddata
	public PlacedBlockData(HashMap<Location, String> placedBlocks) {
		this.placedBlocks = placedBlocks;
	}

	public PlacedBlockData(PlacedBlockData loadedData) {
		this.placedBlocks = loadedData.placedBlocks;
	}

	public boolean saveData(String filePath) {
		try {
			// Saves PlacedBlockData to file
			BukkitObjectOutputStream out = new BukkitObjectOutputStream(
					new GZIPOutputStream(new FileOutputStream(filePath)));
			out.writeObject(this);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static PlacedBlockData loadData(String filePath) {
		try {
			// Loads PlacedBlockData from file
			BukkitObjectInputStream in = new BukkitObjectInputStream(
					new GZIPInputStream(new FileInputStream(filePath)));
			PlacedBlockData data = (PlacedBlockData) in.readObject();
			in.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
