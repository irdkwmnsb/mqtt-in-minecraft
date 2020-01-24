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
            if (lamp.lastPower == -1 || lamp.getBlock().getBlockPower() != lamp.lastPower) {
                lamp.lastPower = lamp.getBlock().getBlockPower();
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            plugin.mqttSend(lamp.name + "/value", lamp.lastPower + "");
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
