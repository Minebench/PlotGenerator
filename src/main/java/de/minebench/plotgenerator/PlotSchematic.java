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

        blocks = new BlockData[getWidth()][getHeight()][getLength()];

        BlockVector3 minimumPoint = clipboard.getMinimumPoint();
        BlockVector3 maximumPoint = clipboard.getMaximumPoint();

        for (int x = minimumPoint.getBlockX(); x < maximumPoint.getBlockX() + 1; x++) {
            for (int z = minimumPoint.getBlockZ(); z < maximumPoint.getBlockZ() + 1; z++) {
                for (int y = minimumPoint.getBlockY(); y < maximumPoint.getBlockY() + 1; y++) {
                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    BlockData adapt = BukkitAdapter.adapt(clipboard.getBlock(pos));

                    BlockVector3 relPos = pos.subtract(minimumPoint);
                    setBlock(relPos.getBlockX(), relPos.getBlockY(), relPos.getBlockZ(), adapt);
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
