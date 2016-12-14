package de.minebench.schematicgenerator;

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
import java.util.logging.Level;

public class SchematicChunkGenerator extends ChunkGenerator {

    private final SchematicGenerator plugin;
    private final CuboidClipboard schematic;

    private final Vector center;

    public SchematicChunkGenerator() {
        plugin = SchematicGenerator.getPlugin(SchematicGenerator.class);
        schematic = null;
        center = new Vector(0,0,0);
    }

    public SchematicChunkGenerator(SchematicGenerator plugin, String id) {
        int z = 0;
        int y = 0;
        int x = 0;
        this.plugin = plugin;
        String args[] = id.split(",");

        if (args.length > 0) {
            schematic = plugin.loadSchematic(args[0]);
        } else {
            schematic = null;
        }

        if (args.length > 3) {
            try {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                plugin.getLogger().log(Level.SEVERE, "Can't load center coordinates from " + id + "!", e);
            }
        } else {
            x = 0;
            y = 0;
            z = 0;
        }
        center = new Vector(x, y, z);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        if (schematic != null && !Vector.ZERO.equals(schematic.getSize())) {
            int startX = (x * 16 + center.getBlockX()) % schematic.getWidth();
            while (startX < 0) {
                startX = schematic.getWidth() + startX;
            }
            int startZ = (z * 16 + center.getBlockZ()) % schematic.getLength();
            while (startZ < 0) {
                startZ = schematic.getLength() + startZ;
            }
            for (int chunkX = 0; chunkX < 16; chunkX++) {
                int schemx = (startX + chunkX) % schematic.getWidth();
                for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                    int schemz = (startZ + chunkZ) % schematic.getLength();
                    for (int chunkY = 0; chunkY < schematic.getHeight(); chunkY++) {
                        BaseBlock block = schematic.getBlock(new Vector(schemx, chunkY, schemz));
                        data.setBlock(schemx, chunkY, schemz, block.getId(), (byte) block.getData());
                    }
                }
            }
        }
        return data;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        Location loc = new Location(world, center.getX(), center.getY(), center.getZ());
        if (!loc.getChunk().isLoaded()) {
            loc.getChunk().load();
        }
        loc.setY(world.getHighestBlockYAt(loc));

        return loc;
    }
}
