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
                } else if ("config".equalsIgnoreCase(parts[0])) {
                    PlotGeneratorConfig config = plugin.getGeneratorConfig(parts[1]);
                    if (config != null) {
                        schematic = config.getSchematic();
                        center = config.getCenter();
                    }
                } else if ("x".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setX(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center x coordinates from " + parts[1] + "!", e);
                    }
                } else if ("y".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setY(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center y coordinates from " + parts[1] + "!", e);
                    }
                } else if ("z".equalsIgnoreCase(parts[0])) {
                    try {
                        center.setZ(Integer.parseInt(parts[1]));
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

        int x = config.getInt("center.x", 0);
        int y = config.getInt("center.y", 0);
        int z = config.getInt("center.z", 0);
        CuboidClipboard schematic = config.contains("schematic") ? plugin.loadSchematic(config.getString("schematic")) : null;

        return new PlotGeneratorConfig(schematic, new Vector(x, y, z));
    }

    public CuboidClipboard getSchematic() {
        return schematic;
    }

    public Vector getCenter() {
        return center;
    }
}
