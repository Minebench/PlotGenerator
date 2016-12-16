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
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class PlotGeneratorConfig {

    private final CuboidClipboard schematic;
    private final BlockVector center;
    private final int overlap;
    private final String regionName;
    private final int regionInset;
    private final int regionMinY;
    private final int regionMaxY;

    public PlotGeneratorConfig(CuboidClipboard schematic, BlockVector center, int overlap, String regionName, int regionInset, int regionMinY, int regionMaxY) {
        this.schematic = schematic;
        this.center = center;
        this.overlap = overlap;
        this.regionName = regionName;
        this.regionInset = regionInset;
        this.regionMinY = regionMinY;
        this.regionMaxY = regionMaxY;
    }

    public static PlotGeneratorConfig fromId(PlotGenerator plugin, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        CuboidClipboard schematic = null;
        BlockVector center = new BlockVector(0, 0, 0);
        String args[] = id.split(",");
        int overlap = 0;
        String regionName = null;
        int regionInset = 0;
        int regionMinY = 0;
        int regionMaxY = 255;

        for (String arg : args) {
            if (!arg.contains("=")) {
                schematic = plugin.loadSchematic(arg);
            } else {
                String[] parts = arg.split("=");
                if ("schem".equalsIgnoreCase(parts[0])) {
                    schematic = plugin.loadSchematic(parts[1]);
                    plugin.getLogger().log(Level.INFO, "Schematic: " + parts[1] + " (size: " + (schematic == null ? "null" : schematic.getSize()) + ")");
                } else if ("config".equalsIgnoreCase(parts[0])) {
                    PlotGeneratorConfig config = plugin.getGeneratorConfig(parts[1]);
                    if (config != null) {
                        plugin.getLogger().log(Level.INFO, "Using config " + parts[1]);
                        schematic = config.getSchematic();
                        center = config.getCenter();
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Config " + parts[1] + " not found?");
                    }
                } else if ("x".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setX(Integer.parseInt(parts[1]));
                        plugin.getLogger().log(Level.INFO, "Center x: " + center.getBlockX());
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center x coordinates from " + parts[1] + "!", e);
                    }
                } else if ("y".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setY(Integer.parseInt(parts[1]));
                        plugin.getLogger().log(Level.INFO, "Center y: " + center.getBlockY());
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center y coordinates from " + parts[1] + "!", e);
                    }
                } else if ("z".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setZ(Integer.parseInt(parts[1]));
                        plugin.getLogger().log(Level.INFO, "Center z: " + center.getBlockZ());
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center z coordinates from " + parts[1] + "!", e);
                    }
                } else if ("overlap".equalsIgnoreCase(parts[0])) {
                    try {
                        overlap = Integer.parseInt(parts[1]);
                        plugin.getLogger().log(Level.INFO, "Overlap: " + overlap);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse overlap from " + parts[1] + "!", e);
                    }
                } else if ("regionName".equalsIgnoreCase(parts[0])) {
                    regionName = parts[1];
                    plugin.getLogger().log(Level.INFO, "Region name: " + regionName);
                } else if ("regionInset".equalsIgnoreCase(parts[0])) {
                    try {
                        regionInset = Integer.parseInt(parts[1]);
                        plugin.getLogger().log(Level.INFO, "Region inset: " + regionInset);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse region inset from " + parts[1] + "!", e);
                    }
                } else if ("regionMinY".equalsIgnoreCase(parts[0])) {
                    try {
                        regionMinY = Integer.parseInt(parts[1]);
                        plugin.getLogger().log(Level.INFO, "Region min y: " + regionInset);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse region min y from " + parts[1] + "!", e);
                    }
                } else if ("regionMaxY".equalsIgnoreCase(parts[0])) {
                    try {
                        regionMaxY = Integer.parseInt(parts[1]);
                        plugin.getLogger().log(Level.INFO, "Region max y: " + regionInset);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse region max y from " + parts[1] + "!", e);
                    }
                }
            }
        }

        return new PlotGeneratorConfig(schematic, center, overlap, regionName, regionInset, regionMinY, regionMaxY);
    }

    public static PlotGeneratorConfig fromConfig(PlotGenerator plugin, ConfigurationSection config) {
        if (config == null) {
            return null;
        }

        plugin.getLogger().log(Level.INFO, "Loading config " + config.getName());

        CuboidClipboard configSchematic = null;
        BlockVector center = new BlockVector(0, 0, 0);
        int overlap = 0;
        String regionName = null;
        int regionInset = 0;
        int regionMinY = 0;
        int regionMaxY = 255;

        if (config.contains("config")) {
            String configName = config.getString("config");
            PlotGeneratorConfig genConfig = plugin.getGeneratorConfig(configName);
            if (genConfig != null) {
                configSchematic = genConfig.getSchematic();
                center = genConfig.getCenter();
                overlap = genConfig.getOverlap();
                regionName = genConfig.getRegionName();
                regionInset = genConfig.getRegionInset();
                regionMinY = genConfig.getRegionMinY();
                regionMaxY = genConfig.getRegionMaxY();
                plugin.getLogger().log(Level.INFO, "Using config " + configName);
            } else {
                plugin.getLogger().log(Level.WARNING, "Config " + configName + " not found?");
            }
        }

        if (config.contains("center.x")) {
            center.setX(config.getInt("center.x"));
            plugin.getLogger().log(Level.INFO, "Center x: " + center.getBlockX());
        }
        if (config.contains("center.y")) {
            center.setY(config.getInt("center.y"));
            plugin.getLogger().log(Level.INFO, "Center y: " + center.getBlockY());
        }
        if (config.contains("center.z")) {
            center.setZ(config.getInt("center.z"));
            plugin.getLogger().log(Level.INFO, "Center z: " + center.getBlockZ());
        }
        if (config.contains("overlap")) {
            overlap = config.getInt("overlap");
            plugin.getLogger().log(Level.INFO, "Overlap: " + overlap);
        }
        if (config.contains("region.name")) {
            regionName = config.getString("region.name");
            plugin.getLogger().log(Level.INFO, "Region name: " + regionName);
        }
        if (config.contains("region.inset")) {
            regionInset = config.getInt("region.inset");
            plugin.getLogger().log(Level.INFO, "Region inset: " + regionInset);
        }
        if (config.contains("region.min-y")) {
            regionMinY = config.getInt("region.min-y");
            plugin.getLogger().log(Level.INFO, "Region min y: " + regionMinY);
        }
        if (config.contains("region.max-y")) {
            regionMaxY = config.getInt("region.max-y");
            plugin.getLogger().log(Level.INFO, "Region max y: " + regionMaxY);
        }

        CuboidClipboard schematic = config.contains("schematic") ? plugin.loadSchematic(config.getString("schematic")) : null;
        if (schematic == null) {
            schematic = configSchematic;
            plugin.getLogger().log(Level.INFO, "Schematic is null." + (schematic != null ? " Using the one from the config option. (Size: " + schematic.getSize() + ")" : ""));
        } else {
            plugin.getLogger().log(Level.INFO, "Schematic: " + config.getString("schematic") + " (size: " + (schematic == null ? "null" : schematic.getSize()) + ")");
        }

        return new PlotGeneratorConfig(schematic, center, overlap, regionName, regionInset, regionMinY, regionMaxY);
    }

    public CuboidClipboard getSchematic() {
        return schematic;
    }

    public BlockVector getCenter() {
        return center;
    }

    public int getOverlap() {
        return overlap;
    }

    public String getRegionName() {
        return regionName;
    }

    public int getRegionInset() {
        return regionInset;
    }

    public int getRegionMinY() {
        return regionMinY;
    }

    public int getRegionMaxY() {
        return regionMaxY;
    }
}
