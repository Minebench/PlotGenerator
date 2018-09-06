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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

public class PlotSchematic {
    private final Vector size;
    private final Vector offset;
    private final Vector origin;

    private final BlockData[][][] blocks;

    public PlotSchematic(CuboidClipboard clipboard) {
        size = clipboard.getSize();
        offset = clipboard.getOffset();
        origin = clipboard.getOrigin();

        blocks = new BlockData[getWidth()][getLength()][getHeight()];

        for (int x = 0; x < getWidth(); x++) {
            for (int z = 0; z < getLength(); z++) {
                for (int y = 0; y < getHeight(); y++) {
                    BlockStateHolder block = clipboard.getBlock(new BlockVector(x, y, z));
                    setBlock(x, y, z, Bukkit.createBlockData(block.getAsString()));
                }
            }
        }

    }

    private void setBlock(int x, int y, int z, BlockData blockData) throws IndexOutOfBoundsException {
        blocks[x][y][z] = blockData;
    }

    public BlockData getBlock(int x, int y, int z) throws IndexOutOfBoundsException {
        return blocks[x][y][z];
    }

    public Vector getSize() {
        return size;
    }

    public Vector getOffset() {
        return offset;
    }

    public Vector getOrigin() {
        return origin;
    }

    public int getWidth() {
        return size.getBlockX();
    }

    public int getLength() {
        return size.getBlockZ();
    }

    public int getHeight() {
        return size.getBlockY();
    }

    @Override
    public String toString() {
        return "PlotSchematic{size=" + size + ",offset =" + offset + ",origin=" + origin + "}";
    }
}
