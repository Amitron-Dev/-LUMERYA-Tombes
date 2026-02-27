package net.amitron.lumerya.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.amitron.lumerya.Tombes;

public class PlayerHandler implements Listener {
	
	private Tombes main;
	
	public PlayerHandler(Tombes main) {
		this.main = main;
	}
	
	@EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        Location loc = p.getLocation().clone().add(0, -1, 0);
        Block block = loc.getBlock();

        block.setType(Material.PLAYER_HEAD);

        if (block.getState() instanceof org.bukkit.block.Skull) {
            org.bukkit.block.Skull skullBlock = (org.bukkit.block.Skull) block.getState();
            skullBlock.setOwningPlayer(p);
            skullBlock.update();
        }
    }

}
