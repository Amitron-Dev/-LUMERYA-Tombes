package net.amitron.lumerya.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import net.amitron.lumerya.Tombes;

public class PlayerHandler implements Listener {
    
    private Tombes main;
    
    public PlayerHandler(Tombes main) {
        this.main = main;
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        String graveId = main.saveStuff(p.getName(), e.getDrops());
        e.getDrops().clear();

        Block block = p.getLocation().getBlock();
        block.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) block.getState();
        skull.setOwningPlayer(p);
        skull.getPersistentDataContainer().set(
            new NamespacedKey(main, "graveId"),
            PersistentDataType.STRING,
            graveId
        );
        skull.update();
    }
    
    @EventHandler
    public void onBreak(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Block block = e.getClickedBlock();
        if (block.getType() != Material.PLAYER_HEAD) return;

        Skull skull = (Skull) block.getState();
        String graveId = skull.getPersistentDataContainer().get(
            new NamespacedKey(main, "graveId"),
            PersistentDataType.STRING
        );
        if (graveId == null) return;

        Location loc = block.getLocation();
        for (ItemStack item : main.getStuffByGraveId(graveId)) {
            loc.getWorld().dropItemNaturally(loc, item);
        }

        main.removeGrave(graveId);
        block.setType(Material.AIR);
    }
}