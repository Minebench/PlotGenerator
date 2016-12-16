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

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.ChrisvA.MbRegionConomy.MbRegionConomy;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class PlotGenerator extends JavaPlugin {

    private WorldEditPlugin worldEdit;
    private WorldGuardPlugin worldGuard;
    private MbRegionConomy regionConomy;
    private File weSchemDir;
    private Map<String, PlotGeneratorConfig> worldConfigs;
    private Set<RegionIntent> regionIntents = new HashSet<>();
    private Map<String, Integer> regionIds = new HashMap<>();

    @Override
    public void onEnable() {
        worldEdit = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        weSchemDir = new File(worldEdit.getDataFolder(), "schematics");
        if (!weSchemDir.exists()) {
            weSchemDir.mkdirs();
        }
        if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuard = WorldGuardPlugin.inst();
        }
        if (getServer().getPluginManager().isPluginEnabled("MbRegionConomy")) {
            regionConomy = MbRegionConomy.getPlugin(MbRegionConomy.class);
        }
        loadConfig();
        getCommand("plotgenerator").setExecutor(new PlotGeneratorCommand(this));
    }

    void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        worldConfigs = new HashMap<>();
        getConfig().getConfigurationSection("worlds").getKeys(false).forEach(this::getGeneratorConfig);
    }


    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new PlotChunkGenerator(this, id);
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }

    public MbRegionConomy getRegionConomy() {
        return regionConomy;
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
        if (regionIntents.isEmpty()) {
            scheduleRegionCreator();
        }
        regionIntents.add(intent);
    }

    private int scheduleRegionCreator() {
        return getServer().getScheduler().runTask(this, () -> {
            Iterator<RegionIntent> intents = regionIntents.iterator();
            while (intents.hasNext()) {
                RegionIntent intent = intents.next();
                intents.remove();
                if (getWorldGuard() != null) {
                    RegionManager manager = getWorldGuard().getRegionManager(intent.getWorld());
                    if (testForRegion(intent)) {
                        continue;
                    }
                    String regionId = getNewRegionId(intent);
                    ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, intent.getMinPoint(), intent.getMaxPoint());
                    manager.addRegion(region);
                    getLogger().log(Level.INFO, "Added new region " + regionId + " at " + intent.getMaxPoint() + " " + intent.getMaxPoint());
                }
            }
        }).getTaskId();
    }

    /**
     * Get a new region id that hasn't been registered with worldguard yet
     * @return
     */
    private String getNewRegionId(RegionIntent intent) {
        RegionManager manager = getWorldGuard().getRegionManager(intent.getWorld());
        String mapKey = intent.getWorld().getName() + "_" + intent.getRegionName();
        int idNumber = 1;
        if (regionIds.containsKey(mapKey)) {
            idNumber = regionIds.get(mapKey);
            idNumber++;
        }

        String regionName;
        do {
            regionName = intent.getRegionName().replace("%world%", intent.getWorld().getName());
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
        RegionManager manager = getWorldGuard().getRegionManager(intent.getWorld());
        String replacedRegionName = intent.getRegionName().replace("%world%", "").replace("%number%", "");
        ApplicableRegionSet minPointRegions = manager.getApplicableRegions(intent.getMinPoint());
        for (ProtectedRegion region : minPointRegions.getRegions()) {
            if (getStartSimilarity(region.getId(), replacedRegionName) > replacedRegionName.length() / 2) {
                return true;
            }
        }
        ApplicableRegionSet maxPointRegions = manager.getApplicableRegions(intent.getMaxPoint());
        for (ProtectedRegion region : maxPointRegions.getRegions()) {
            if (getStartSimilarity(region.getId(), replacedRegionName) > replacedRegionName.length() / 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check how similar the starts two strings
     * @param string1
     * @param string2
     * @return The length that matches
     */
    private int getStartSimilarity(String string1, String string2) {
        if (string1.equalsIgnoreCase(string2)) {
            return string1.length();
        }
        int i = 0;
        String s1l = string1.toLowerCase();
        String s2l = string2.toLowerCase();
        while (s1l.startsWith(s2l.substring(0, i)) && i <= s2l.length()) {
            i++;
        }
        return i;
    }

}
