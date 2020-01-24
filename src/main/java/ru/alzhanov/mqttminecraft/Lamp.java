package ru.alzhanov.mqttminecraft;

import com.google.gson.annotations.JsonAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class Lamp {
    @JsonAdapter(LocationSerializer.class)
    Location location;
    String name;
    int color;
    String armorStandUUID;

    private transient ArmorStand armorStand;
    transient int lastPower = -1;
    private transient Block block;
    transient static Material[] MATERIAL_GLASS_MAP = {
            Material.BLACK_STAINED_GLASS,       // 0
            Material.RED_STAINED_GLASS,         // 1
            Material.GREEN_STAINED_GLASS,       // 2
            Material.BROWN_STAINED_GLASS,       // 3
            Material.BLUE_STAINED_GLASS,        // 4
            Material.PURPLE_STAINED_GLASS,      // 5
            Material.CYAN_STAINED_GLASS,        // 6
            Material.LIGHT_GRAY_STAINED_GLASS,  // 7
            Material.GRAY_STAINED_GLASS,        // 8
            Material.PINK_STAINED_GLASS,        // 9
            Material.LIME_STAINED_GLASS,        // 10
            Material.YELLOW_STAINED_GLASS,      // 11
            Material.LIGHT_BLUE_STAINED_GLASS,  // 12
            Material.MAGENTA_STAINED_GLASS,     // 13
            Material.ORANGE_STAINED_GLASS,      // 14
            Material.WHITE_STAINED_GLASS        // 15
    };

    public Lamp(String name, Block block, int color) {
        this.name = name;
        this.location = block.getLocation();
        this.color = color;
        this.armorStand = getArmorStand();
    }

    public ArmorStand getArmorStand() {
        if (armorStand == null) {
            for (Entity entity : location.getWorld().getEntities()) {
                if (entity.getUniqueId().toString().equals(armorStandUUID)) {
                    armorStand = (ArmorStand) entity;
                    break;
                }
            }
            if (armorStand == null) {
                armorStand = location.getWorld().spawn(location.clone().add(0.5, -0.118, 0.5), ArmorStand.class);
                armorStandUUID = armorStand.getUniqueId().toString(); // Should call a save, but who cares?
            }
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);
            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.setCustomName(name);
            armorStand.setCustomNameVisible(false);
            armorStand.setSmall(true);
            armorStand.setFireTicks(Integer.MAX_VALUE);
            armorStand.setHelmet(new ItemStack(MATERIAL_GLASS_MAP[color]));
        }
        return armorStand;
    }

    public Block getBlock() {
        if(block == null)
            block = location.getBlock();
        return block;
    }
}
