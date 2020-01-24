package ru.alzhanov.mqttminecraft;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class LocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject el = new JsonObject();
        el.addProperty("x", location.getBlockX());
        el.addProperty("y", location.getBlockY());
        el.addProperty("z", location.getBlockZ());
        el.addProperty("world", location.getWorld().getName());
        return el;
    }

    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new Location(Bukkit.getWorld(jsonElement.getAsJsonObject().get("world").getAsString()),
                jsonElement.getAsJsonObject().get("x").getAsInt(),
                jsonElement.getAsJsonObject().get("y").getAsInt(),
                jsonElement.getAsJsonObject().get("z").getAsInt());
    }
}
