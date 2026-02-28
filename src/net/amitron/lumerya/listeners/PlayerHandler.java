package net.amitron.lumerya.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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

        if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) p.getLastDamageCause();
            if (evt.getDamager() instanceof Player) {
                // PvP → drop normal
                return;
            }
        }

        // Toutes les autres morts → spawn tombe
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
    public void onEntityExplosion(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> {
            if (block.getType() != Material.PLAYER_HEAD) return false;
            if (!(block.getState() instanceof Skull)) return false;

            Skull skull = (Skull) block.getState();
            String graveId = skull.getPersistentDataContainer().get(
                new NamespacedKey(main, "graveId"),
                PersistentDataType.STRING
            );
            return graveId != null; // true = protège la tête, false = laisse détruire
        });
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> {
            if (block.getType() != Material.PLAYER_HEAD) return false;
            if (!(block.getState() instanceof Skull)) return false;

            Skull skull = (Skull) block.getState();
            String graveId = skull.getPersistentDataContainer().get(
                new NamespacedKey(main, "graveId"),
                PersistentDataType.STRING
            );
            return graveId != null;
        });
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

        String ownerName = main.getOwnerByGraveId(graveId); // nom du joueur mort

        Location loc = block.getLocation();

        // Drop du stuff
        for (ItemStack item : main.getStuffByGraveId(graveId)) {
            loc.getWorld().dropItemNaturally(loc, item);
        }

        // Particules
        loc.getWorld().spawnParticle(
            Particle.SOUL,
            loc.clone().add(0.5, 0.5, 0.5),
            20,
            0.3, 0.3, 0.3,
            0.01
        );

        // Message au joueur
        e.getPlayer().sendMessage(
            "§7☠ Vous avez récupéré le stuff de §c" + ownerName
        );

        main.removeGrave(graveId);
        block.setType(Material.AIR);
    }
}