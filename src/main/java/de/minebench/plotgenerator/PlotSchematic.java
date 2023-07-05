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
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class PlotSchematic {
    private final Clipboard clipboard;
    private final BlockVector3 dimensions;

    public PlotSchematic(Clipboard clipboard) {
        this.clipboard = clipboard;
        this.dimensions = clipboard.getDimensions();
    }

    public BlockData getBlock(int x, int y, int z) {
        BlockVector3 pos = BlockVector3.at(x, y, z).add(clipboard.getMinimumPoint());
        if (!pos.containedWithin(clipboard.getMinimumPoint(), clipboard.getMaximumPoint())) {
            return Material.AIR.createBlockData();
        }

        BlockState blockState = clipboard.getBlock(pos);
        return BukkitAdapter.adapt(blockState);
    }

    public BlockVector3 getSize() {
        return dimensions;
    }

    public BlockVector3 getOrigin() {
        return clipboard.getOrigin();
    }

    public int getWidth() {
        return dimensions.getBlockX();
    }

    public int getLength() {
        return dimensions.getBlockZ();
    }

    public int getHeight() {
        return dimensions.getBlockY();
    }

    public int getMinY() {
        return clipboard.getMinimumPoint().getBlockY();
    }

    public int getMaxY() {
        return clipboard.getMaximumPoint().getBlockY();
    }

    @Override
    public String toString() {
        return "PlotSchematic{size=" + getSize() + ",origin=" + getOrigin() + "}";
    }
}
