package ru.alzhanov.mqttminecraft;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.UUID;
import java.util.logging.Logger;

public class MQTTPlugin extends JavaPlugin implements org.bukkit.event.Listener {
    FileConfiguration config = getConfig();
    MqttClientPersistence clientPersistence = null;
    Logger logger = getLogger();
    protected IMqttClient client = null;
    public Database db = null;

    public void loadConfiguration() {
        config.addDefault("MQTT.Broker.Url", "ADDME");
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void mqttConnect() throws IllegalArgumentException, MqttException {
        String publisherId = UUID.randomUUID().toString();
        clientPersistence = new MqttDefaultFilePersistence(getDataFolder().getAbsolutePath());
        client = new MqttClient(config.getString("MQTT.Broker.Url"), publisherId, clientPersistence);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(4);
        if (config.isString("MQTT.Broker.Password")) {
            logger.info(String.format("Using password %s", config.getString("MQTT.Broker.Password")));
            options.setPassword(config.getString("MQTT.Broker.Password").toCharArray());
        }
        if (config.isString("MQTT.Broker.Username")) {
            logger.info(String.format("Using username %s", config.getString("MQTT.Broker.Username")));
            options.setUserName(config.getString("MQTT.Broker.Username"));
        }
        client.connect(options);
    }

    public boolean mqttIsConnected() {
        return client != null && client.isConnected();
    }

    public void mqttDisconnect() throws MqttException {
        client.disconnect();
        clientPersistence.close();
    }

    public void mqttSend(String topic, String payload) throws MqttException {
        if(!mqttIsConnected()) {
            mqttConnect();
        }
        MqttMessage msg = new MqttMessage(payload.getBytes());
        msg.setQos(1);
        msg.setRetained(true);
        client.publish(topic, msg);
    }

    @Override
    public void onEnable() {
        logger.info("MQTT is enabling");
        loadConfiguration();
        try {
            mqttConnect();
            MqttMessage msg = new MqttMessage("Plugin is enabled".getBytes());
            msg.setQos(0);
            msg.setRetained(true);
            client.publish("minecraft/status", msg);
        } catch (MqttException e) {
            e.printStackTrace();
            logger.severe("Unable to connect to broker");
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid broker url. Please specify it in the config.");
        }
        db = new Database(getDataFolder());
        this.getCommand("mqtt").setExecutor(new CommandMQTT(this));
        this.getCommand("mqttconfigure").setExecutor(new CommandConfig(this));
        this.getCommand("mqttconf").setExecutor(new CommandConfig(this));
        this.getCommand("mqttconfig").setExecutor(new CommandConfig(this));
        this.getCommand("mqttbroker").setExecutor(new CommandConfig(this));
        getServer().getPluginManager().registerEvents(new MyListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, new EveryTickRunnable(this),0, 1);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        try {
            if (mqttIsConnected()) {
                mqttSend("minecraft/status", "Plugin disabling");
                mqttDisconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
