package ru.alzhanov.mqttminecraft;

import org.bukkit.Bukkit;
import org.eclipse.paho.client.mqttv3.MqttException;

public class EveryTickRunnable implements Runnable {
    MQTTPlugin plugin;

    public EveryTickRunnable(MQTTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (final Lamp lamp : plugin.db.lamps) {
//            if (lamp.lastPower == -1 || lamp.getBlock().getBlockPower() != lamp.lastPower) {
//            int newPower = lamp.getBlock().getBlockPower()
            if (lamp.lastPower == -1 || lamp.getBlock().getBlockPower() != lamp.lastPower) {
                lamp.lastPower = lamp.getBlock().getBlockPower();
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        plugin.mqttSend(lamp.name + "/value", String.valueOf(lamp.lastPower));
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
