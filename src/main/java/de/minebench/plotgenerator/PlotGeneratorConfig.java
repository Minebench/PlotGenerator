package de.minebench.plotgenerator;

/*
 * PlotGenerator
 * Copyright (c) 2018 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.base.Suppliers;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Supplier;
import java.util.logging.Level;

public class PlotGeneratorConfig {

    private final String id;
    private final Supplier<PlotSchematic> schematic;
    private final BlockVector3 center;
    private final int overlap;
    private final String regionId;
    private final int regionInset;
    private final int regionMinY;
    private final int regionMaxY;
    private final double regionPrice;
    private final String plotType;

    public PlotGeneratorConfig(String id, Supplier<PlotSchematic> schematic, BlockVector3 center, int overlap, String regionId, int regionInset, int regionMinY, int regionMaxY, double regionPrice, String plotType) {
        this.id = id;
        this.schematic = Suppliers.memoize(schematic::get);
        this.center = center;
        this.overlap = overlap;
        this.regionId = regionId;
        this.regionInset = regionInset;
        this.regionMinY = regionMinY;
        this.regionMaxY = regionMaxY;
        this.regionPrice = regionPrice;
        this.plotType = plotType;
    }

    public static PlotGeneratorConfig fromId(PlotGenerator plugin, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        Builder b = new Builder(plugin, id);

        String args[] = id.split(",");

        for (String arg : args) {
            if (!arg.contains("=")) {
                b.schematic(arg);
            } else {
                String[] parts = arg.split("=");
                if ("schem".equalsIgnoreCase(parts[0])) {
                    b.schematic(parts[1]);
                } else if ("config".equalsIgnoreCase(parts[0])) {
                    b.copy(parts[1]);
                } else if ("x".equalsIgnoreCase(parts[0])) {
                    try {
                        b.centerX(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center x coordinates from " + parts[1] + "!", e);
                    }
                } else if ("y".equalsIgnoreCase(parts[0])) {
                    try {
                        b.centerY(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center y coordinates from " + parts[1] + "!", e);
                    }
                } else if ("z".equalsIgnoreCase(parts[0])) {
                    try {
                        b.centerZ(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse center z coordinates from " + parts[1] + "!", e);
                    }
                } else if ("overlap".equalsIgnoreCase(parts[0])) {
                    try {
                        b.overlap(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse overlap from " + parts[1] + "!", e);
                    }
                } else if ("regionId".equalsIgnoreCase(parts[0])) {
                    b.regionId(parts[1]);
                } else if ("regionInset".equalsIgnoreCase(parts[0])) {
                    try {
                        b.regionInset(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse region inset from " + parts[1] + "!", e);
                    }
                } else if ("regionMinY".equalsIgnoreCase(parts[0])) {
                    try {
                        b.regionMinY(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse region min y from " + parts[1] + "!", e);
                    }
                } else if ("regionMaxY".equalsIgnoreCase(parts[0])) {
                    try {
                        b.regionMaxY(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse region max y from " + parts[1] + "!", e);
                    }
                } else if ("regionPrice".equalsIgnoreCase(parts[0])) {
                    try {
                        b.regionPrice(Double.parseDouble(parts[1]));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.SEVERE, "Can't parse land price from " + parts[1] + "!", e);
                    }
                } else if ("plotType".equalsIgnoreCase(parts[0])) {
                    b.plotType(parts[1]);
                }
            }
        }

        return b.build();
    }

    public static PlotGeneratorConfig fromConfig(PlotGenerator plugin, ConfigurationSection config) {
        if (config == null) {
            return null;
        }

        plugin.getLogger().log(Level.INFO, "Loading config " + config.getName());

        Builder b = new Builder(plugin, config.getName());

        if (config.contains("config")) {
            b.copy(config.getString("config"));
        }
        if (config.contains("schematic")) {
            b.schematic(config.getString("schematic"));
        }
        if (config.contains("center.x")) {
            b.centerX(config.getInt("center.x"));
        }
        if (config.contains("center.y")) {
            b.centerY(config.getInt("center.y"));
        }
        if (config.contains("center.z")) {
            b.centerZ(config.getInt("center.z"));
        }
        if (config.contains("overlap")) {
            b.overlap(config.getInt("overlap"));
        }
        if (config.contains("region.id")) {
            b.regionId(config.getString("region.id"));
        }
        if (config.contains("region.inset")) {
            b.regionInset(config.getInt("region.inset"));
        }
        if (config.contains("region.min-y")) {
            b.regionMinY(config.getInt("region.min-y"));
        }
        if (config.contains("region.max-y")) {
            b.regionMaxY(config.getInt("region.max-y"));
        }
        if (config.contains("region.price")) {
            b.regionPrice(config.getDouble("region.price"));
        }
        if (config.contains("plotsigns.type")) {
            b.plotType(config.getString("plotsigns.type"));
        }

        return b.build();
    }

    public PlotSchematic getSchematic() {
        return schematic.get();
    }

    public BlockVector3 getCenter() {
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

    public double getRegionPrice() {
        return regionPrice;
    }

    public String getPlotType() {
        return plotType;
    }

    public String getId() {
        return id;
    }

    public static class Builder {

        private String id;
        private Supplier<PlotSchematic> schematic = Suppliers.ofInstance(null);
        private BlockVector3 center = BlockVector3.at(0, 0, 0);
        private int overlap = 0;
        private String regionId = null;
        private int regionInset = 0;
        private int regionMinY = 0;
        private int regionMaxY = 255;
        private double regionPrice = -1;
        private String plotType = null;
        private double landPrice = -1;
        private String landPermission = null;
        private PlotGenerator plugin;

        public Builder(PlotGenerator plugin, String id) {
            this.plugin = plugin;
            this.id = id;
        }

        public Builder schematic(String name) {
            return this.schematic(name, plugin.loadSchematic(name)::join);
        }

        public Builder schematic(String name, Supplier<PlotSchematic> schematicSupplier) {
            this.schematic = () -> {
                PlotSchematic schematic = schematicSupplier.get();
                if (schematic == null) {
                    plugin.getLogger().log(Level.WARNING, "Schematic " + name + "not found?");
                    return null;
                }

                plugin.getLogger().log(Level.INFO, "Schematic: " + name + " (size: " + schematic.getSize() + ")");
                return schematic;
            };

            return this;
        }

        public Builder center(BlockVector3 center) {
            this.center = center;
            plugin.getLogger().log(Level.INFO, "Center: " + center);
            return this;
        }

        public Builder centerX(int x) {
            center = center.withX(x);
            plugin.getLogger().log(Level.INFO, "Center x: " + x);
            return this;
        }

        public Builder centerY(int y) {
            center = center.withY(y);
            plugin.getLogger().log(Level.INFO, "Center y: " + y);
            return this;
        }

        public Builder centerZ(int z) {
            center = center.withZ(z);
            plugin.getLogger().log(Level.INFO, "Center z: " + z);
            return this;
        }

        public Builder overlap(int overlap) {
            this.overlap = overlap;
            plugin.getLogger().log(Level.INFO, "Overlap: " + overlap);
            return this;
        }

        public Builder regionId(String regionId) {
            this.regionId = regionId;
            plugin.getLogger().log(Level.INFO, "Region id: " + regionId);
            return this;
        }

        public Builder regionInset(int regionInset) {
            this.regionInset = regionInset;
            plugin.getLogger().log(Level.INFO, "Region inset: " + regionInset);
            return this;
        }

        public Builder regionMinY(int regionMinY) {
            this.regionMinY = regionMinY;
            plugin.getLogger().log(Level.INFO, "Region min y: " + regionMinY);
            return this;
        }

        public Builder regionMaxY(int regionMaxY) {
            this.regionMaxY = regionMaxY;
            plugin.getLogger().log(Level.INFO, "Region max y: " + regionMaxY);
            return this;
        }

        public Builder regionPrice(double regionPrice) {
            this.regionPrice = regionPrice;
            plugin.getLogger().log(Level.INFO, "Region price: " + regionPrice);
            return this;
        }

        public Builder plotType(String plotType) {
            this.plotType = plotType;
            plugin.getLogger().log(Level.INFO, "PlotSigns plot type: " + plotType);
            return this;
        }

        public Builder landPrice(double landPrice) {
            this.landPrice = landPrice;
            plugin.getLogger().log(Level.INFO, "MbRegionConomy land price: " + landPrice);
            return this;
        }

        public Builder landPermission(String landPermission) {
            this.landPermission = landPermission;
            plugin.getLogger().log(Level.INFO, "MbRegionConomy land permission: " + landPermission);
            return this;
        }

        public PlotGeneratorConfig build() {
            return new PlotGeneratorConfig(id, schematic, center, overlap, regionId, regionInset, regionMinY, regionMaxY, regionPrice, plotType);
        }

        public Builder copy(PlotGeneratorConfig config) {
            schematic = config::getSchematic;
            center = config.getCenter();
            overlap = config.getOverlap();
            regionId = config.getRegionId();
            regionInset = config.getRegionInset();
            regionMinY = config.getRegionMinY();
            regionMaxY = config.getRegionMaxY();
            regionPrice = config.getRegionPrice();
            plotType = config.getPlotType();
            return this;
        }

        public Builder copy(String configName) {
            PlotGeneratorConfig config = plugin.getGeneratorConfig(configName);
            if (config != null) {
                plugin.getLogger().log(Level.INFO, "Using config " + configName);
                return copy(config);
            } else {
                plugin.getLogger().log(Level.WARNING, "Config " + configName + " not found?");
            }
            return this;
        }
    }
}
