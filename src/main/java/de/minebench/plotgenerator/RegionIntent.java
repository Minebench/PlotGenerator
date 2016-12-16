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
    private final String regionId;
    private final BlockVector minPoint;
    private final BlockVector maxPoint;
    private BlockVector landSign = null;
    private double landPrice = - 1;
    private String landPermission = "";

    public RegionIntent(World world, String regionId, BlockVector minPoint, BlockVector maxPoint) {
        this.world = world;
        this.regionId = regionId;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public World getWorld() {
        return world;
    }

    public String getRegionId() {
        return regionId;
    }

    public BlockVector getMinPoint() {
        return minPoint;
    }

    public BlockVector getMaxPoint() {
        return maxPoint;
    }

    @Override
    public String toString() {
        return "RegionIntent{world=" + world.getName() + ",regionId=" + regionId + ",minPoint=" + minPoint + ",maxPoint=" + maxPoint + ",landSign=" + landSign + ",landPrice=" + landPrice + ",landPermission=" + landPermission + "}";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public void setLandSign(BlockVector regionConomySign) {
        this.landSign = regionConomySign;
    }

    public BlockVector getLandSign() {
        return landSign;
    }

    public void setLandPrice(double landPrice) {
        this.landPrice = landPrice;
    }

    public double getLandPrice() {
        return landPrice;
    }

    public void setLandPermission(String landPermission) {
        this.landPermission = landPermission;
    }

    public String getLandPermission() {
        return landPermission;
    }
}
