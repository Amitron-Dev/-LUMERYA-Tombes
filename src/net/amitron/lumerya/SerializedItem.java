package net.amitron.lumerya;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class SerializedItem {
	public String graveId;
    public String player;
    public String type;
    public String itemBase64;
    public int amount;

    public static String serialize(ItemStack item) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
        oos.writeObject(item);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static ItemStack deserialize(String base64) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64);
        BukkitObjectInputStream ois = new BukkitObjectInputStream(new ByteArrayInputStream(data));
        ItemStack item = (ItemStack) ois.readObject();
        ois.close();
        return item;
    }
}