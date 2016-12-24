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
