package org.pythonchik.creeperheal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class CreeperHeal extends JavaPlugin implements Listener {
    private List<List<Object>> explodedBlocks = new ArrayList<>();
    private int currentIndex = 0;
    private Logger logger = this.getLogger();
    private Map<Location, ItemStack[]> chestContents = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onCommandUse(PlayerCommandPreprocessEvent event) {
        logger.info(event.getMessage());
        if (event.getPlayer().getName().equals("_Foxya_") || event.getPlayer().getName().equals("Orange") || event.getPlayer().getName().equals("_UwU_Ch4n_")) return;
        if ((event.getMessage().contains("tp") || (event.getMessage().contains("teleport"))) && (event.getMessage().contains("_Foxya_") || event.getMessage().contains("Orange") || event.getMessage().contains("_UwU_Ch4n_"))) {
            event.setMessage("/kick @s");
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType().equals(EntityType.CREEPER)) {
            for (Block block : event.blockList()) {
                List<Object> entry = new ArrayList<>();
                entry.add(block.getType());
                entry.add(block.getBlockData());
                entry.add(block.getLocation());
                if (block.getType() == Material.CHEST) {
                    Chest chest = (Chest) block.getState();
                    chestContents.put(chest.getLocation(), chest.getBlockInventory().getContents());
                    chest.getInventory().clear(); // Clear chest contents
                }
                explodedBlocks.add(entry);
                block.setType(Material.AIR); // Prevent block dropping
            }

            Bukkit.getScheduler().runTaskLater(this, this::restoreBlocks, 100); // Delayed block restoration
        }
    }

    private void restoreBlocks() {
        if (currentIndex < explodedBlocks.size()) {
            List<Object> entry = explodedBlocks.get(currentIndex);
            Location location = (Location) entry.get(2);
            Material type = (Material) entry.get(0);
            BlockData blockData = (BlockData) entry.get(1);

            location.getWorld().getBlockAt(location).setType(type);
            location.getWorld().getBlockAt(location).setBlockData(blockData);

            if (type == Material.CHEST) {
                Chest chest = (Chest) location.getBlock().getState();
                Inventory chestInventory = chest.getBlockInventory();
                ItemStack[] contents = chestContents.get(location);
                if (contents != null) {
                    chestInventory.setContents(contents);
                    chestContents.remove(location);
                }
            }
            currentIndex++;
            Bukkit.getScheduler().runTaskLater(this, this::restoreBlocks, 60); // Delayed next block restoration
        } else {
            explodedBlocks.clear();
            currentIndex = 0;
        }
    }
}


