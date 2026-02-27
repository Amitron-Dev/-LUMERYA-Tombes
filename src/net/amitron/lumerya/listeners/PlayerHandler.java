package net.amitron.lumerya.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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

import net.amitron.lumerya.Tombes;

public class PlayerHandler implements Listener {
    
    private Tombes main;
    
    public PlayerHandler(Tombes main) {
        this.main = main;
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().clear();
        Player p = e.getEntity();
        Location loc = p.getLocation().clone().add(0, -1, 0);
        Block block = loc.getBlock();
        block.setType(Material.PLAYER_HEAD);
        if (block.getState() instanceof Skull) {
            Skull skullBlock = (Skull) block.getState();
            skullBlock.setOwningPlayer(p);
            skullBlock.update();
        }
    }
    
    @EventHandler
    public void onBreak(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Block block = e.getClickedBlock();
        if (block.getType() != Material.PLAYER_HEAD) return;
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;
        if (!(block.getState() instanceof Skull)) return;
        Skull skullBlock = (Skull) block.getState();
        OfflinePlayer owner = skullBlock.getOwningPlayer();
        if (owner == null) return;
        Location loc = block.getLocation();
        List<ItemStack> stuff = main.getStuff(owner.getName());
        if (stuff != null) {
            for (ItemStack item : stuff) {
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }
        block.setType(Material.AIR);
    }
}