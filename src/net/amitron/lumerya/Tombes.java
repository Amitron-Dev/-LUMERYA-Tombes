package net.amitron.lumerya;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
        try (FileReader reader = new FileReader(stuffFile)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<SerializedItem>>() {}.getType();
            List<SerializedItem> allStuff = gson.fromJson(reader, type);
            List<ItemStack> playerStuff = new ArrayList<>();
            for (SerializedItem si : allStuff) {
                if (si.player.equals(playerName)) {
                    playerStuff.add(si.toItemStack());
                }
            }
            return playerStuff;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
