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
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class PlotGeneratorConfig {

    private final CuboidClipboard schematic;
    private final Vector center;

    public PlotGeneratorConfig(CuboidClipboard schematic, Vector center) {
        this.schematic = schematic;
        this.center = center;
    }

    public static PlotGeneratorConfig fromId(PlotGenerator plugin, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        CuboidClipboard schematic = null;
        Vector center = new Vector(0, 0, 0);
        String args[] = id.split(",");

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
                        plugin.getLogger().log(Level.INFO, "Center x:" + center.getBlockX());
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center x coordinates from " + parts[1] + "!", e);
                    }
                } else if ("y".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setY(Integer.parseInt(parts[1]));
                        plugin.getLogger().log(Level.INFO, "Center y:" + center.getBlockY());
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center y coordinates from " + parts[1] + "!", e);
                    }
                } else if ("z".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setZ(Integer.parseInt(parts[1]));
                        plugin.getLogger().log(Level.INFO, "Center z:" + center.getBlockZ());
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center z coordinates from " + parts[1] + "!", e);
                    }
                }
            }
        }

        return new PlotGeneratorConfig(schematic, center);
    }

    public static PlotGeneratorConfig fromConfig(PlotGenerator plugin, ConfigurationSection config) {
        if (config == null) {
            return null;
        }

        plugin.getLogger().log(Level.INFO, "Loading config " + config.getName());

        CuboidClipboard configSchematic = null;
        Vector center = new Vector(0, 0, 0);
        if (config.contains("config")) {
            String configName = config.getString("config");
            PlotGeneratorConfig genConfig = plugin.getGeneratorConfig(configName);
            if (genConfig != null) {
                configSchematic = genConfig.getSchematic();
                center = genConfig.getCenter();
                plugin.getLogger().log(Level.INFO, "Using config " + configName);
            } else {
                plugin.getLogger().log(Level.WARNING, "Config " + configName + " not found?");
            }
        }

        if (config.contains("center.x")) {
            center.setX(config.getInt("center.x"));
            plugin.getLogger().log(Level.INFO, "Center x:" + center.getBlockX());
        }
        if (config.contains("center.y")) {
            center.setY(config.getInt("center.y"));
            plugin.getLogger().log(Level.INFO, "Center y:" + center.getBlockY());
        }
        if (config.contains("center.z")) {
            center.setZ(config.getInt("center.z"));
            plugin.getLogger().log(Level.INFO, "Center z:" + center.getBlockZ());
        }

        CuboidClipboard schematic = config.contains("schematic") ? plugin.loadSchematic(config.getString("schematic")) : null;
        if (schematic == null) {
            schematic = configSchematic;
            plugin.getLogger().log(Level.INFO, "Schematic is null." + (schematic != null ? " Using the one from the config option. (Size: " + schematic.getSize() + ")" : ""));
        } else {
            plugin.getLogger().log(Level.INFO, "Schematic: " + config.getString("schematic") + " (size: " + (schematic == null ? "null" : schematic.getSize()) + ")");
        }

        return new PlotGeneratorConfig(schematic, center);
    }

    public CuboidClipboard getSchematic() {
        return schematic;
    }

    public Vector getCenter() {
        return center;
    }
}
