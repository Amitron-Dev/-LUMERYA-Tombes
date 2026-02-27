package net.amitron.lumerya;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class SerializedItem {
    public String player;
    public String type;
    public int amount;

    public ItemStack toItemStack() {
        Material mat = Material.getMaterial(type);
        if (mat == null) mat = Material.STONE;
        return new ItemStack(mat, amount);
    }
}