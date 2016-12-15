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
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.ChrisvA.MbRegionConomy.MbRegionConomy;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class PlotGenerator extends JavaPlugin {

    private WorldEditPlugin worldEdit;
    private WorldGuardPlugin worldGuard;
    private MbRegionConomy regionConomy;
    private File weSchemDir;
    private Map<String, PlotGeneratorConfig> worldConfigs;

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
}
