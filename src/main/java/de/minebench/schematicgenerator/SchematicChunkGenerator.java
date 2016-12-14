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
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class SchematicChunkGenerator extends ChunkGenerator {

    private final SchematicGenerator plugin;
    private final CuboidClipboard schematic;

    private final int x;
    private final int y;
    private final int z;

    public SchematicChunkGenerator() {
        plugin = SchematicGenerator.getPlugin(SchematicGenerator.class);
        schematic = null;
        x = 0;
        y = 0;
        z = 0;
    }

    public SchematicChunkGenerator(SchematicGenerator plugin, String id) {
        int z1 = 0;
        int y1 = 0;
        int x1 = 0;
        this.plugin = plugin;
        String args[] = id.split(",");

        if (args.length > 0) {
            schematic = plugin.loadSchematic(args[0]);
        } else {
            schematic = null;
        }

        if (args.length > 3) {
            try {
                x1 = Integer.parseInt(args[1]);
                y1 = Integer.parseInt(args[2]);
                z1 = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                plugin.getLogger().log(Level.SEVERE, "Can't load center coordinates from " + id + "!", e);
            }
        } else {
            x1 = 0;
            y1 = 0;
            z1 = 0;
        }
        z = z1;
        y = y1;
        x = x1;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        int xStart = x * 16 % schematic.getWidth();
        int zStart = z * 16 % schematic.getLength();
        for (int ix = 0; ix < 16; ix++) {
            int schemx = (xStart + ix) % schematic.getWidth();
            for (int iz = 0; iz < 16; iz++) {
                int schemz = (zStart + iz) % schematic.getLength();
                for (int iy = 0; iy < schematic.getHeight(); iy++) {
                    BaseBlock block = schematic.getBlock(new Vector(schemx, iy, schemz));
                    data.setBlock(schemx, iy, schemz, block.getId(), (byte) block.getData());
                }
            }
        }
        return data;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        Location loc = new Location(world, x, y, z);
        if (!loc.getChunk().isLoaded()) {
            loc.getChunk().load();
        }
        loc.setY(world.getHighestBlockYAt(loc));

        return loc;
    }
}
