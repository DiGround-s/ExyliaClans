package net.diground.exylia.utils;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Base64;


public class MigrationUtils {

    private static final Gson gson = new Gson();

//    public static ItemStack[] convertJsonDataToItemStacks(String jsonData) throws JsonParseException {
//        JsonArray itemsArray = JsonParser.parseString(jsonData).getAsJsonArray();
//        ItemStack[] items = new ItemStack[itemsArray.size()];
//
//        for (int i = 0; i < itemsArray.size(); i++) {
//            JsonObject itemObject = itemsArray.get(i).getAsJsonObject();
//            Material material = Material.matchMaterial(itemObject.get("type").getAsString());
//            int amount = itemObject.get("amount").getAsInt();
//            ItemStack item = new ItemStack(material, amount);
//
//            if (itemObject.has("meta")) {
//                JsonObject metaObject = itemObject.get("meta").getAsJsonObject();
//                ItemMeta meta = item.getItemMeta();
//                // Configurar el ItemMeta según el JSON
//                // (ej. nombre, lore, etc.)
//                item.setItemMeta(meta);
//            }
//
//            items[i] = item;
//        }
//
//        return items;
//    }

    public static String convertItemStacksToJsonData(ItemStack[] items) {
        JsonArray itemsArray = new JsonArray();

        for (ItemStack item : items) {
            JsonObject itemObject = new JsonObject();
            itemObject.addProperty("type", item.getType().name());
            itemObject.addProperty("amount", item.getAmount());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                JsonObject metaObject = new JsonObject();
                // Configurar ItemMeta según el JSON
                // (ej. nombre, lore, etc.)
                itemObject.add("meta", metaObject);
            }

            itemsArray.add(itemObject);
        }

        return itemsArray.toString();
    }

    public static String serializeItemStacks(ItemStack[] items) {
        JsonArray itemsArray = new JsonArray();

        for (ItemStack item : items) {
            if (item != null) {
                JsonObject itemObject = new JsonObject();
                itemObject.addProperty("type", item.getType().name());
                itemObject.addProperty("amount", item.getAmount());

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    JsonObject metaObject = new JsonObject();
                    // Configurar ItemMeta según el JSON
                    // (ej. nombre, lore, etc.)
                    itemObject.add("meta", metaObject);
                }

                itemsArray.add(itemObject);
            }
        }

        return Base64.getEncoder().encodeToString(itemsArray.toString().getBytes());
    }
}
