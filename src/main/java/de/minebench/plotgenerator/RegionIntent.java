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
import org.bukkit.World;

class RegionIntent {
    private final World world;
    private PlotGeneratorConfig config;
    private final BlockVector minPoint;
    private final BlockVector maxPoint;
    private BlockVector sign = null;

    public RegionIntent(World world, PlotGeneratorConfig config, BlockVector minPoint, BlockVector maxPoint) {
        this.world = world;
        this.config = config;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public World getWorld() {
        return world;
    }

    public PlotGeneratorConfig getConfig() {
        return config;
    }

    public BlockVector getMinPoint() {
        return minPoint;
    }

    public BlockVector getMaxPoint() {
        return maxPoint;
    }

    @Override
    public String toString() {
        return "RegionIntent{world=" + world.getName() + ",config =" + config.getId() + ",minPoint=" + minPoint + ",maxPoint=" + maxPoint + ",sign=" + sign + "}";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public void setSign(BlockVector regionConomySign) {
        this.sign = regionConomySign;
    }

    public BlockVector getSign() {
        return sign;
    }

}
