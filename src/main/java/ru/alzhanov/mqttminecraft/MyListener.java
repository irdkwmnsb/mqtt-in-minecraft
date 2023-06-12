package ru.alzhanov.mqttminecraft;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.regex.Pattern;
//import static ru.alzhanov.mqttminecraft.Lamp.MATERIAL_GLASS_MAP;

public class MyListener implements Listener {
    MQTTPlugin plugin;
//    static Material[] dyes = {};

    public MyListener(MQTTPlugin plugin) {
        this.plugin = plugin;
    }

//    private static final HashMap<Material, Integer> MATERIAL_DYE_MAP = new HashMap<Material, Integer>() {
//        {
//            put(Material.BLACK_DYE, 0);       // 0
//            put(Material.RED_DYE, 1);         // 1
//            put(Material.GREEN_DYE, 2);       // 2
//            put(Material.BROWN_DYE, 3);       // 3
//            put(Material.BLUE_DYE, 4);        // 4
//            put(Material.PURPLE_DYE, 5);      // 5
//            put(Material.CYAN_DYE, 6);        // 6
//            put(Material.LIGHT_GRAY_DYE, 7);  // 7
//            put(Material.GRAY_DYE, 8);        // 8
//            put(Material.PINK_DYE, 9);        // 9
//            put(Material.LIME_DYE, 10);        // 10
//            put(Material.YELLOW_DYE, 11);      // 11
//            put(Material.LIGHT_BLUE_DYE, 12);  // 12
//            put(Material.MAGENTA_DYE, 13);     // 13
//            put(Material.ORANGE_DYE, 14);      // 14
//            put(Material.WHITE_DYE, 15);      // 15
//        }
//    };

    private static boolean isRedstoneLamp(Material m) {
        return m == Material.REDSTONE_LAMP_ON || m == Material.REDSTONE_LAMP_OFF;
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getMaterial() == Material.NAME_TAG &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                isRedstoneLamp(event.getClickedBlock().getType())) {
            if (!event.getPlayer().hasPermission("mqtt.lamp.create")) {
                event.getPlayer().sendMessage("You don't have enough permissions to do this.");
                return;
            }
            String name = event.getItem().getItemMeta().getDisplayName();
            if (!Pattern.compile("[a-zA-Z0-9_/]+").matcher(name).matches()) {
                event.getPlayer().sendMessage("Cannot create a lamp with forbidden characters");
                return;
            }
            for (Lamp lamp : plugin.db.lamps) {
                if (lamp.name.equals(name)) {
                    event.getPlayer().sendMessage("Lamp with such name exists.");
                    return;
                }
                if (lamp.location.equals(event.getClickedBlock().getLocation())) {
                    event.getPlayer().sendMessage("This lamp is already registered.");
                    return;
                }
            }
            Lamp newLamp = new Lamp(name,
                    event.getClickedBlock(),
                    new Dye(DyeColor.WHITE));
            plugin.db.lamps.add(newLamp);
            plugin.db.save();
            try {
                plugin.mqttSend(name + "/color", String.valueOf(newLamp.color));
                plugin.mqttSend(name + "/value", String.valueOf(event.getClickedBlock().getBlockPower()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
            event.getPlayer().sendMessage(String.format("Added lamp %s.", name));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                isRedstoneLamp(event.getClickedBlock().getType())) {
            Material clickedMaterial = event.getMaterial();
            if (clickedMaterial == Material.INK_SACK) { // Ink sack because that's how it was before 1.13
                if (!event.getPlayer().hasPermission("mqtt.lamp.color")) {
                    event.getPlayer().sendMessage("You don't have enough permissions to do this.");
                    return;
                }
                Dye color = (Dye) event.getItem().getData();
                for (Lamp lamp : plugin.db.lamps) {
                    if (lamp.location.equals(event.getClickedBlock().getLocation())) {
                        lamp.color = color;
                        lamp.getArmorStand().setHelmet(new ItemStack(Material.STAINED_GLASS, 1, DyeColor.getByDyeData(color.getData()).getWoolData()));
                        plugin.db.save();
                        try {
                            plugin.mqttSend(lamp.name + "/color", String.valueOf(lamp.color.getColor().ordinal()));
                        } catch (MqttException e) {
                            e.printStackTrace();
                            event.getPlayer().sendMessage("Could not send message to broker. Check console for further details.");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isRedstoneLamp(event.getBlock().getType())) {
            for (Lamp lamp : plugin.db.lamps) {
                if (lamp.location.equals(event.getBlock().getLocation())) {
                    if(!event.getPlayer().hasPermission("mqtt.lamp.break")) {
                        event.getPlayer().sendMessage("You don't have enough permissions to do this.");
                        event.setCancelled(true);
                        return;
                    }
                    lamp.getArmorStand().remove();
                    plugin.db.lamps.remove(lamp);
                    break;
                }
            }
            plugin.db.save();
        }
    }
}
