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

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
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
        if (config == null) {
            return data;
        }

        PlotSchematic schematic = config.getSchematic();
        if (schematic == null || BlockVector3.ZERO.equals(schematic.getSize())) {
            return data;
        }

        BlockVector3 center = config.getCenter();

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

        int startY = Math.max(-64, center.getBlockY());
        int height = Math.min(startY + schematic.getHeight(), world.getMaxHeight());

        BlockVector3 sign = null;
        for (int chunkX = 0; chunkX < 16; chunkX++) {
            int schemX = (startX + chunkX) % width;
            for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                int schemZ = (startZ + chunkZ) % length;
                for (int chunkY = startY; chunkY < height; chunkY++) {
                    BlockData block = schematic.getBlock(schemX, chunkY - startY, schemZ);
                    data.setBlock(chunkX, chunkY, chunkZ, block);
                    if (sign == null && (block instanceof Sign || block instanceof WallSign)) {
                        sign = BlockVector3.at(x * 16 + chunkX, chunkY, z * 16 + chunkZ);
                    }
                }
            }
        }

        if (plugin.getWorldGuard() != null && config.getRegionId() != null) {
            BlockVector3 minPoint = BlockVector3.at(
                    x * 16 - startX + config.getRegionInset(),
                    config.getRegionMinY(),
                    z * 16 - startZ + config.getRegionInset()
            );
            BlockVector3 maxPoint = BlockVector3.at(
                    minPoint.getBlockX() + width - 2 * config.getRegionInset(),
                    config.getRegionMaxY(),
                    minPoint.getBlockZ() + length - 2 * config.getRegionInset()
            );
            RegionIntent intent = new RegionIntent(world, config, minPoint, maxPoint);
            if (sign != null ) {
                intent.setSign(sign);
            }
            plugin.registerRegionIntent(intent);
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
