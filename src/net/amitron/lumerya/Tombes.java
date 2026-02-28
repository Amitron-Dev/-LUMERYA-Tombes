package net.amitron.lumerya;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.amitron.lumerya.listeners.PlayerHandler;

public class Tombes extends JavaPlugin {
	
	private File stuffFile;
	
	@Override
	public void onEnable() {
		
		// Instances
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHandler(this), this);
		
		stuffFile = new File(getDataFolder(), "stuff.json");
        if (!stuffFile.exists()) {
            stuffFile.getParentFile().mkdirs();
            saveResource("stuff.json", false);
        }
		
	}
	
	
	public List<ItemStack> getStuff(String playerName) {
	    List<ItemStack> result = new ArrayList<>();
	    
	    try (FileReader reader = new FileReader(stuffFile)) {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<SerializedItem>>() {}.getType();
	        
	        
	        List<SerializedItem> allStuff = gson.fromJson(reader, type);
	        if (allStuff == null) return result;

	        for (SerializedItem si : allStuff) {
	            if (si.player.equalsIgnoreCase(playerName)) {
	                result.add(SerializedItem.deserialize(si.itemBase64));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	
	
	public String saveStuff(String playerName, List<ItemStack> drops) {
	    try {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<SerializedItem>>() {}.getType();
	        List<SerializedItem> allStuff = new ArrayList<>();

	        if (stuffFile.exists() && stuffFile.length() > 0) {
	            try (FileReader reader = new FileReader(stuffFile)) {
	                List<SerializedItem> existing = gson.fromJson(reader, type);
	                if (existing != null) allStuff.addAll(existing);
	            }
	        }

	        String graveId = java.util.UUID.randomUUID().toString();

	        for (ItemStack item : drops) {
	            if (item == null || item.getType() == Material.AIR) continue;

	            SerializedItem si = new SerializedItem();
	            si.graveId = graveId;
	            si.player = playerName;
	            si.itemBase64 = SerializedItem.serialize(item);
	            allStuff.add(si);
	        }

	        try (FileWriter writer = new FileWriter(stuffFile, false)) {
	            gson.toJson(allStuff, writer);
	        }

	        return graveId;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public List<ItemStack> getStuffByGraveId(String graveId) {
	    List<ItemStack> result = new ArrayList<>();
	    try (FileReader reader = new FileReader(stuffFile)) {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<SerializedItem>>() {}.getType();
	        List<SerializedItem> allStuff = gson.fromJson(reader, type);
	        if (allStuff == null) return result;

	        for (SerializedItem si : allStuff) {
	            if (graveId.equals(si.graveId)) {
	                result.add(SerializedItem.deserialize(si.itemBase64));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	public void removeGrave(String graveId) {
	    try {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<SerializedItem>>() {}.getType();

	        List<SerializedItem> allStuff = new ArrayList<>();
	        try (FileReader reader = new FileReader(stuffFile)) {
	            List<SerializedItem> existing = gson.fromJson(reader, type);
	            if (existing != null) {
	                for (SerializedItem si : existing) {
	                    if (!graveId.equals(si.graveId)) {
	                        allStuff.add(si);
	                    }
	                }
	            }
	        }

	        try (FileWriter writer = new FileWriter(stuffFile, false)) {
	            gson.toJson(allStuff, writer);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
