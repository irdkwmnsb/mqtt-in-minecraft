package ru.alzhanov.mqttminecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eclipse.paho.client.mqttv3.MqttException;

public class CommandMQTT implements CommandExecutor {
    MQTTPlugin plugin;

    public CommandMQTT(MQTTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 2) {
            return false;
        }
        if(!plugin.mqttIsConnected()) {
            try {
                plugin.mqttConnect();
            } catch (MqttException e) {
                commandSender.sendMessage("Error while connecting. Check server logs.");
                return true;
            }
        }
        try {
            StringBuilder sbStr = new StringBuilder();
            for (int i = 1, il = strings.length; i < il; i++) {
                if (i > 1)
                    sbStr.append(" ");
                sbStr.append(strings[i]);
            }
            plugin.mqttSend(strings[0], sbStr.toString());
            commandSender.sendMessage(String.format("Sent %d bytes to %s", sbStr.length(), strings[0]));
        } catch (MqttException e) {
            e.printStackTrace();
            commandSender.sendMessage("An error occured while sending your message. Please check server log.");
        }
        return true;
    }
}
