package de.minebench.plotgenerator;

/*
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 *
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.minebench.plotgenerator.commands.BuyPlotCommand;
import de.minebench.plotgenerator.commands.PlotGeneratorCommand;
import me.ChrisvA.MbRegionConomy.MbRegionConomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class PlotGenerator extends JavaPlugin {

    private WorldGuardPlugin worldGuard = null;
    private MbRegionConomy regionConomy = null;
    private Economy economy;
    private File weSchemDir;
    private Map<String, PlotGeneratorConfig> worldConfigs;
    private Map<RegionIntent, Boolean> regionIntents = new ConcurrentHashMap<>();
    private Map<String, Integer> regionIds = new HashMap<>();
    private int regionCreatorTask = -1;

    @Override
    public void onEnable() {
        WorldEditPlugin worldEdit = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        weSchemDir = new File(worldEdit.getDataFolder(), "schematics");
        if (!weSchemDir.exists()) {
            weSchemDir.mkdirs();
        }
        loadConfig();
        getCommand("plotgenerator").setExecutor(new PlotGeneratorCommand(this));
        getCommand("buyplot").setExecutor(new BuyPlotCommand(this));
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        worldConfigs = new HashMap<>();
        getConfig().getConfigurationSection("worlds").getKeys(false).forEach(this::getGeneratorConfig);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new PlotChunkGenerator(this, id);
    }

    public WorldGuardPlugin getWorldGuard() {
        if (worldGuard == null && getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuard = WorldGuardPlugin.inst();
        }
        return worldGuard;
    }

    public MbRegionConomy getRegionConomy() {
        if (regionConomy == null && getServer().getPluginManager().isPluginEnabled("MbRegionConomy")) {
            regionConomy = MbRegionConomy.getPlugin(MbRegionConomy.class);
        }
        return regionConomy;
    }

    public Economy getEconomy() {
        if (economy == null) {
            if (getServer().getPluginManager().isPluginEnabled("Vault")) {
                return null;
            }
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return null;
            }
            economy = rsp.getProvider();
        }
        return economy;
    }

    public CuboidClipboard loadSchematic(String schematicName) {
        if (schematicName == null || schematicName.isEmpty()) {
            return null;
        }

        File file = new File(getDataFolder(), schematicName + ".schematic");
        if (!file.exists()){
            file = new File(weSchemDir, schematicName + ".schematic");
        }
        if (!file.exists()) {
            getLogger().log(Level.SEVERE, "No schematic found with the name " + schematicName + "!");
            return null;
        }

        SchematicFormat schemFormat = SchematicFormat.getFormat(file);
        if (schemFormat == null) {
            getLogger().log(Level.SEVERE, "Could not load schematic format from file " + file.getAbsolutePath() + "!");
            return null;
        }
        try {
            return schemFormat.load(file);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading file " + file.getAbsolutePath(), e);
            return null;
        }
    }

    public PlotGeneratorConfig getGeneratorConfig(World world) {
        return getGeneratorConfig(world.getName());
    }

    public PlotGeneratorConfig getGeneratorConfig(String worldName) {
        if (worldConfigs.containsKey(worldName)) {
            return worldConfigs.get(worldName);
        }
        ConfigurationSection config = getConfig().getConfigurationSection("worlds." + worldName);
        PlotGeneratorConfig info = PlotGeneratorConfig.fromConfig(this, config);
        if (info != null) {
            worldConfigs.put(worldName, info);
        }
        return info;
    }

    public void registerRegionIntent(RegionIntent intent) {
        if (regionCreatorTask == -1) {
            regionCreatorTask = scheduleRegionCreator();
        }
        regionIntents.put(intent, true);
    }

    private int scheduleRegionCreator() {
        return getServer().getScheduler().runTask(this, () -> {
            Iterator<Map.Entry<RegionIntent, Boolean>> intents = regionIntents.entrySet().iterator();
            while (intents.hasNext()) {
                RegionIntent intent = intents.next().getKey();
                intents.remove();
                if (getWorldGuard() != null) {
                    RegionManager manager = getWorldGuard().getRegionManager(intent.getWorld());
                    if (testForRegion(intent)) {
                        if (intent.getLandSign() != null) {
                            registerRegionConomySign(intent);
                        }
                        continue;
                    }
                    String regionId = getNewRegionId(intent);
                    ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, intent.getMinPoint(), intent.getMaxPoint());

                    double tpX = region.getMinimumPoint().getX() + (region.getMaximumPoint().getX() - region.getMinimumPoint().getX()) / 2;
                    double tpZ = region.getMaximumPoint().getZ();
                    double tpY = intent.getWorld().getHighestBlockYAt((int) tpX, (int) tpZ) + 1;
                    region.setFlag(DefaultFlag.TELE_LOC, new Location(BukkitUtil.getLocalWorld(intent.getWorld()), new Vector(tpX,tpY,tpZ), 180, 0));
                    if (intent.getRegionPrice() > 0) {
                        region.setFlag(DefaultFlag.BUYABLE, true);
                        region.setFlag(DefaultFlag.PRICE, intent.getRegionPrice());
                    }

                    manager.addRegion(region);
                    getLogger().log(Level.INFO, "Added new region " + regionId + " at " + intent.getMinPoint() + " " + intent.getMaxPoint());
                    if (intent.getLandSign() != null) {
                        registerRegionConomySign(intent);
                    }
                }
            }
            regionCreatorTask = -1;
        }).getTaskId();
    }

    private void registerRegionConomySign(RegionIntent intent) {
        if (getRegionConomy() == null || getWorldGuard() == null || intent.getLandPrice() < 0) {
            return;
        }

        ProtectedRegion region = getSimilarRegion(intent, intent.getLandSign());

        if (region == null) {
            getLogger().log(Level.WARNING, "Sign was found at " + intent.getLandSign() + " but no region?");
            return;
        }

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            region.setFlag(DefaultFlag.BUYABLE, true);
            getRegionConomy().getRegionDatabase().insertRegion(intent.getWorld().getName(), region.getId(), intent.getLandPrice());
            if (intent.getLandPermission() != null && !intent.getLandPermission().isEmpty()) {
                getRegionConomy().getRegionDatabase().updatePermission(intent.getWorld().getName(), region.getId(), intent.getLandPermission());
            }
            getLogger().log(Level.INFO, "Made region " + intent.getWorld().getName() + "/" + region.getId() + " buyable. Price: " + intent.getLandPrice() + (intent.getLandPermission() != null ? ", permission: " + intent.getLandPermission() : ""));

            String[] lines = new String[4];
            lines[0] = getRegionConomy().getConf().getSignSell();
            lines[1] = region.getId();
            lines[2] = String.valueOf(intent.getLandPrice());
            lines[3] = intent.getLandPermission();
            getServer().getScheduler().runTask(this, () -> {
                Block block = intent.getWorld().getBlockAt(intent.getLandSign().getBlockX(), intent.getLandSign().getBlockY(), intent.getLandSign().getBlockZ());
                if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    for (int i = 0; i < lines.length; i++) {
                        sign.setLine(i, lines[i]);
                    }
                    sign.update();
                    getLogger().log(Level.INFO, "Wrote sign for region " + intent.getWorld().getName() + "/" + region.getId() + " at " + intent.getLandSign());
                }
            });
        });
    }

    /**
     * Get a new region id that hasn't been registered with worldguard yet
     * @return
     */
    private String getNewRegionId(RegionIntent intent) {
        RegionManager manager = getWorldGuard().getRegionManager(intent.getWorld());
        String mapKey = intent.getWorld().getName() + "_" + intent.getRegionId();
        int idNumber = 0;
        if (regionIds.containsKey(mapKey)) {
            idNumber = regionIds.get(mapKey);
        }

        String regionName;
        do {
            idNumber++;
            regionName = intent.getRegionId().replace("%world%", intent.getWorld().getName());
            regionName = regionName.contains("%number%") ? regionName.replace("%number%", String.valueOf(idNumber)) : regionName + idNumber;
        } while (manager.getRegion(regionName) != null);

        regionIds.put(mapKey, idNumber);
        return regionName;
    }

    /**
     * Test whether or not there already is a similar region
     * @param intent
     * @return
     */
    private boolean testForRegion(RegionIntent intent) {
        return getSimilarRegion(intent, intent.getMinPoint()) != null || getSimilarRegion(intent, intent.getMaxPoint()) != null;
    }

    /**
     * Get a region at that location that is similar to the intent
     * @param intent
     * @return
     */
    private ProtectedRegion getSimilarRegion(RegionIntent intent, BlockVector loc) {
        Pattern regionRegex = Pattern.compile("^" + intent.getRegionId().replace("%world%", "\\w+").replace("%number%", "\\d+") + "$");
        RegionManager manager = getWorldGuard().getRegionManager(intent.getWorld());
        ApplicableRegionSet regions = manager.getApplicableRegions(loc);
        for (ProtectedRegion region : regions) {
            if (regionRegex.matcher(region.getId()).matches()) {
                return region;
            }
        }
        return null;
    }
}
