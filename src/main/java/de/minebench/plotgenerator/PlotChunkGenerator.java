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
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class PlotChunkGenerator extends ChunkGenerator {

    private final PlotGenerator plugin;
    private final PlotGeneratorConfig config;

    public PlotChunkGenerator() {
        plugin = PlotGenerator.getPlugin(PlotGenerator.class);
        config = null;
    }

    public PlotChunkGenerator(PlotGenerator plugin, String id) {
        this.plugin = plugin;
        config = PlotGeneratorConfig.fromId(plugin, id);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        PlotGeneratorConfig config = getConfig(world);
        if (config != null && config.getSchematic() != null && !BlockVector.ZERO.equals(config.getSchematic().getSize())) {
            CuboidClipboard schematic = config.getSchematic();
            BlockVector center = config.getCenter();
            int width = schematic.getWidth() - config.getOverlap();
            int startX = (x * 16 - center.getBlockX()) % width;
            while (startX < 0) {
                startX = width + startX;
            }
            int length = schematic.getLength() - config.getOverlap();
            int startZ = (z * 16 - center.getBlockZ()) % length;
            while (startZ < 0) {
                startZ = length + startZ;
            }

            BlockVector sign = null;
            for (int chunkX = 0; chunkX < 16; chunkX++) {
                int schemX = (startX + chunkX) % width;
                for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                    int schemZ = (startZ + chunkZ) % length;
                    for (int chunkY = 0; chunkY < schematic.getHeight(); chunkY++) {
                        BaseBlock block = schematic.getBlock(new BlockVector(schemX, chunkY, schemZ));
                        data.setBlock(chunkX, chunkY, chunkZ, block.getId(), (byte) block.getData());
                        if (sign == null && block.getId() == Material.SIGN_POST.getId() || block.getId() == Material.WALL_SIGN.getId()) {
                            sign = new BlockVector(x * 16 + chunkX, chunkY, z * 16 + chunkZ);
                        }
                    }
                }
            }

            if (plugin.getWorldGuard() != null && config.getRegionId() != null) {
                BlockVector minPoint = new BlockVector(
                        x * 16 - startX + config.getRegionInset(),
                        config.getRegionMinY(),
                        z * 16 - startZ + config.getRegionInset()
                );
                BlockVector maxPoint = new BlockVector(
                        minPoint.getBlockX() + width - 2 * config.getRegionInset(),
                        config.getRegionMaxY(),
                        minPoint.getBlockZ() + length - 2 * config.getRegionInset()
                );
                RegionIntent intent = new RegionIntent(world, config.getRegionId(), minPoint, maxPoint, config.getRegionPrice());
                if (sign != null && plugin.getRegionConomy() != null) {
                    intent.setLandSign(sign);
                    intent.setLandPrice(config.getLandPrice());
                    intent.setLandPermission(config.getLandPermission());
                }
                plugin.registerRegionIntent(intent);
            }
        }
        return data;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        if (getConfig(world) == null) {
            return null;
        }
        Location loc = new Location(world, getConfig(world).getCenter().getX(), getConfig(world).getCenter().getY(), getConfig(world).getCenter().getZ());
        if (!loc.getChunk().isLoaded()) {
            loc.getChunk().load();
        }
        loc.setY(world.getHighestBlockYAt(loc));

        return loc;
    }

    public PlotGeneratorConfig getConfig(World world) {
        if (config == null) {
            return plugin.getGeneratorConfig(world);
        }
        return config;
    }
}
