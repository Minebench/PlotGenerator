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
    private final String regionId;
    private final int regionInset;
    private final int regionMinY;
    private final int regionMaxY;
    private final double landPrice;
    private final String landPermission;

    public PlotGeneratorConfig(CuboidClipboard schematic, BlockVector center, int overlap, String regionId, int regionInset, int regionMinY, int regionMaxY, double landPrice, String landPermission) {
        this.schematic = schematic;
        this.center = center;
        this.overlap = overlap;
        this.regionId = regionId;
        this.regionInset = regionInset;
        this.regionMinY = regionMinY;
        this.regionMaxY = regionMaxY;
        this.landPrice = landPrice;
        this.landPermission = landPermission;
    }

    public static PlotGeneratorConfig fromId(PlotGenerator plugin, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        CuboidClipboard schematic = null;
        BlockVector center = new BlockVector(0, 0, 0);
        String args[] = id.split(",");
        int overlap = 0;
        String regionId = null;
        int regionInset = 0;
        int regionMinY = 0;
        int regionMaxY = 255;
        double landPrice = -1;
        String landPermission = null;

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
                } else if ("regionId".equalsIgnoreCase(parts[0])) {
                    regionId = parts[1];
                    plugin.getLogger().log(Level.INFO, "Region id: " + regionId);
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
                } else if ("landPrice".equalsIgnoreCase(parts[0])) {
                    try {
                        landPrice = Double.parseDouble(parts[1]);
                        plugin.getLogger().log(Level.INFO, "MbRegionConomy land price: " + landPrice);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse land price from " + parts[1] + "!", e);
                    }
                } else if ("landPermission".equalsIgnoreCase(parts[0])) {
                    landPermission = parts[1];
                    plugin.getLogger().log(Level.INFO, "MbRegionConomy land permission: " + regionInset);
                }
            }
        }

        return new PlotGeneratorConfig(schematic, center, overlap, regionId, regionInset, regionMinY, regionMaxY, landPrice, landPermission);
    }

    public static PlotGeneratorConfig fromConfig(PlotGenerator plugin, ConfigurationSection config) {
        if (config == null) {
            return null;
        }

        plugin.getLogger().log(Level.INFO, "Loading config " + config.getName());

        CuboidClipboard configSchematic = null;
        BlockVector center = new BlockVector(0, 0, 0);
        int overlap = 0;
        String regionId = null;
        int regionInset = 0;
        int regionMinY = 0;
        int regionMaxY = 255;
        double langPrice = -1;
        String landPermission = null;

        if (config.contains("config")) {
            String configName = config.getString("config");
            PlotGeneratorConfig genConfig = plugin.getGeneratorConfig(configName);
            if (genConfig != null) {
                configSchematic = genConfig.getSchematic();
                center = genConfig.getCenter();
                overlap = genConfig.getOverlap();
                regionId = genConfig.getRegionId();
                regionInset = genConfig.getRegionInset();
                regionMinY = genConfig.getRegionMinY();
                regionMaxY = genConfig.getRegionMaxY();
                langPrice = genConfig.getLandPrice();
                landPermission = genConfig.getLandPermission();
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
        if (config.contains("region.id")) {
            regionId = config.getString("region.id");
            plugin.getLogger().log(Level.INFO, "Region id: " + regionId);
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
        if (config.contains("land.price")) {
            langPrice = config.getDouble("land.price");
            plugin.getLogger().log(Level.INFO, "MbRegionConomy land price: " + langPrice);
        }
        if (config.contains("land.permission")) {
            landPermission = config.getString("land.permission");
            plugin.getLogger().log(Level.INFO, "MbRegionConomy land permission: " + landPermission);
        }

        CuboidClipboard schematic = config.contains("schematic") ? plugin.loadSchematic(config.getString("schematic")) : null;
        if (schematic == null) {
            schematic = configSchematic;
            plugin.getLogger().log(Level.INFO, "Schematic is null." + (schematic != null ? " Using the one from the config option. (Size: " + schematic.getSize() + ")" : ""));
        } else {
            plugin.getLogger().log(Level.INFO, "Schematic: " + config.getString("schematic") + " (size: " + (schematic == null ? "null" : schematic.getSize()) + ")");
        }

        return new PlotGeneratorConfig(schematic, center, overlap, regionId, regionInset, regionMinY, regionMaxY, langPrice, landPermission);
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

    public String getRegionId() {
        return regionId;
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

    public double getLandPrice() {
        return landPrice;
    }

    public String getLandPermission() {
        return landPermission;
    }
}
