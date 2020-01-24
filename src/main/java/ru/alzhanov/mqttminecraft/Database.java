package ru.alzhanov.mqttminecraft;

import com.google.gson.Gson;
import jdk.internal.jline.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Database {
    public ArrayList<Lamp> lamps;
    File dbFile = null;

    public Database(File dataFolder) {
        dbFile = new File(dataFolder, "lamps.json");
        try {
            if (dbFile.createNewFile()) {
                FileWriter writer = new FileWriter(dbFile);
                writer.write("[]");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public void load() {
        try {
            Gson gson = new Gson();
            FileReader fr = new FileReader(dbFile);
            lamps = new ArrayList<Lamp>(Arrays.asList(gson.fromJson(fr, Lamp[].class)));
            fr.close();
            for(Lamp lamp: lamps) {
                if(lamp.location.getBlock().getType() != Material.REDSTONE_LAMP) {
                    lamp.location.getBlock().setType(Material.REDSTONE_LAMP);
                }
                lamp.getArmorStand();
            }
        } catch (FileNotFoundException e) { // how in the fucking world would that happen ??
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Gson gson = new Gson();
            FileWriter fw = new FileWriter(dbFile);
            gson.toJson(lamps.toArray(), fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
