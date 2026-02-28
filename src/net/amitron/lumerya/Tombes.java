package net.amitron.lumerya;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.amitron.lumerya.listeners.PlayerHandler;

public class Tombes extends JavaPlugin {
	
	private File stuffFile;
	private File gravesFile;
	
	@Override
	public void onEnable() {
		
		// Instances
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHandler(this), this);
		
		stuffFile = new File(getDataFolder(), "stuff.json");
        if (!stuffFile.exists()) {
            stuffFile.getParentFile().mkdirs();
            saveResource("stuff.json", false);
        }
		
        
        gravesFile = new File(getDataFolder(), "graves.json");
        if (!gravesFile.exists()) {
            gravesFile.getParentFile().mkdirs();
            saveResource("graves.json", false);
        }

        loadGraves();
        
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
	
	public void saveGrave(String graveId, Player player, Location loc) {
	    try {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<GraveData>>() {}.getType();
	        List<GraveData> graves = new ArrayList<>();

	        if (gravesFile.exists() && gravesFile.length() > 0) {
	            try (FileReader reader = new FileReader(gravesFile)) {
	                List<GraveData> existing = gson.fromJson(reader, type);
	                if (existing != null) graves.addAll(existing);
	            }
	        }

	        GraveData gd = new GraveData();
	        gd.graveId = graveId;
	        gd.owner = player.getUniqueId().toString();
	        gd.owner = player.getName();
	        gd.world = loc.getWorld().getName();
	        gd.x = loc.getBlockX();
	        gd.y = loc.getBlockY();
	        gd.z = loc.getBlockZ();

	        graves.add(gd);

	        try (FileWriter writer = new FileWriter(gravesFile, false)) {
	            gson.toJson(graves, writer);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadGraves() {
	    try (FileReader reader = new FileReader(gravesFile)) {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<GraveData>>() {}.getType();
	        List<GraveData> graves = gson.fromJson(reader, type);
	        if (graves == null) return;

	        for (GraveData gd : graves) {
	            World world = Bukkit.getWorld(gd.world);
	            if (world == null) continue;

	            Location loc = new Location(world, gd.x, gd.y, gd.z);
	            Block block = loc.getBlock();
	            block.setType(Material.PLAYER_HEAD);

	            Skull skull = (Skull) block.getState();

	            UUID ownerUUID;
	            try {
	                ownerUUID = UUID.fromString(gd.owner);
	            } catch (IllegalArgumentException ex) {
	                continue; // skip si UUID invalide
	            }

	            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
	            skull.setOwningPlayer(offlinePlayer);
	            skull.getPersistentDataContainer().set(
	                new NamespacedKey(this, "graveId"),
	                PersistentDataType.STRING,
	                gd.graveId
	            );
	            skull.update();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
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
	        Type type = new TypeToken<List<GraveData>>() {}.getType();

	        List<GraveData> graves = new ArrayList<>();
	        try (FileReader reader = new FileReader(gravesFile)) {
	            List<GraveData> existing = gson.fromJson(reader, type);
	            if (existing != null) {
	                for (GraveData g : existing) {
	                    if (!g.graveId.equals(graveId)) {
	                        graves.add(g);
	                    }
	                }
	            }
	        }

	        try (FileWriter writer = new FileWriter(gravesFile, false)) {
	            gson.toJson(graves, writer);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public String getOwnerByGraveId(String graveId) {
	    try (FileReader reader = new FileReader(stuffFile)) {
	        Gson gson = new Gson();
	        Type type = new TypeToken<List<SerializedItem>>() {}.getType();

	        List<SerializedItem> allStuff = gson.fromJson(reader, type);
	        if (allStuff == null) return null;

	        for (SerializedItem si : allStuff) {
	            if (graveId.equals(si.graveId)) {
	                return si.player;
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}
