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
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Location;
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
        if (getConfig(world) != null && getConfig(world).getSchematic() != null && !Vector.ZERO.equals(getConfig(world).getSchematic().getSize())) {
            CuboidClipboard schematic = getConfig(world).getSchematic();
            Vector center = getConfig(world).getCenter();
            int startX = (x * 16 + center.getBlockX()) % schematic.getWidth();
            while (startX < 0) {
                startX = schematic.getWidth() + startX;
            }
            int startZ = (z * 16 + center.getBlockZ()) % schematic.getLength();
            while (startZ < 0) {
                startZ = schematic.getLength() + startZ;
            }
            for (int chunkX = 0; chunkX < 16; chunkX++) {
                int schemX = (startX + chunkX) % schematic.getWidth();
                for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                    int schemZ = (startZ + chunkZ) % schematic.getLength();
                    for (int chunkY = 0; chunkY < schematic.getHeight(); chunkY++) {
                        BaseBlock block = schematic.getBlock(new Vector(schemX, chunkY, schemZ));
                        data.setBlock(chunkX, chunkY, chunkZ, block.getId(), (byte) block.getData());
                    }
                }
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
