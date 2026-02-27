package net.amitron.lumerya;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.amitron.lumerya.listeners.PlayerHandler;

public class Tombes extends JavaPlugin {
	
	@Override
	public void onEnable() {
		
		// Instances
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerHandler(this), this);
		
		
	}

}
