package ru.alzhanov.mqttminecraft;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eclipse.paho.client.mqttv3.MqttException;

public class CommandConfig implements CommandExecutor {
    private final MQTTPlugin plugin;

    public CommandConfig(MQTTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length < 1)
            return false;
        plugin.config.set("MQTT.Broker.Url", strings[0]);
        if(strings.length > 1)
            plugin.config.set("MQTT.Broker.Password", strings[1]);
        else
            plugin.config.set("MQTT.Broker.Password", null);
        if(strings.length > 2)
            plugin.config.set("MQTT.Broker.Username", strings[2]);
        else
            plugin.config.set("MQTT.Broker.Username", null);
        plugin.saveConfig();
        commandSender.sendMessage("Updated settings successfully. Reconnecting...");
        try {
            if(plugin.mqttIsConnected())
                plugin.mqttDisconnect();
            plugin.mqttConnect();
            commandSender.sendMessage("Connected successfully.");
        } catch (MqttException e) {
            e.printStackTrace();
            commandSender.sendMessage("Unable to connect to broker.");
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage("It seems as if the URL is incorrect. Please try again.");
        }
        return true;
    }
}
