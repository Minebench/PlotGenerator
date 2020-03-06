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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.block.data.BlockData;

public class PlotSchematic {
    private final BlockVector3 size;
    private final BlockVector3 origin;

    private final BlockData[][][] blocks;

    public PlotSchematic(Clipboard clipboard) {
        size = clipboard.getDimensions();
        origin = clipboard.getOrigin();

        blocks = new BlockData[getWidth()][getLength()][getHeight()];

        for (int x = 0; x < getWidth(); x++) {
            for (int z = 0; z < getLength(); z++) {
                for (int y = 0; y < getHeight(); y++) {
                    setBlock(x, y, z, BukkitAdapter.adapt(clipboard.getBlock(BlockVector3.at(x, y, z))));
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

    public BlockVector3 getSize() {
        return size;
    }

    public BlockVector3 getOrigin() {
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
        return "PlotSchematic{size=" + size + ",origin=" + origin + "}";
    }
}
