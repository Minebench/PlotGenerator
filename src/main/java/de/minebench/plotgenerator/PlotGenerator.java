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

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.minebench.plotgenerator.commands.BuyPlotCommand;
import de.minebench.plotgenerator.commands.PlotGeneratorCommand;
import de.minebench.plotsigns.PlotSigns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class PlotGenerator extends JavaPlugin {

    private WorldGuardPlugin worldGuard = null;
    private PlotSigns plotSigns;

    private Economy economy;

    private File weSchemDir;
    private Map<String, PlotGeneratorConfig> worldConfigs;
    private Map<RegionIntent, Boolean> regionIntents = new ConcurrentHashMap<>();
    private Map<String, Integer> regionIds = new HashMap<>();
    private int regionCreatorTask = -1;

    public static BooleanFlag BUYABLE_FLAG = new BooleanFlag("buyable");
    public static DoubleFlag PRICE_FLAG = new DoubleFlag("price");

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            BUYABLE_FLAG = registerOrGetFlag(BUYABLE_FLAG);
            PRICE_FLAG = registerOrGetFlag(PRICE_FLAG);
        }
    }

    private <T extends Flag> T registerOrGetFlag(T flag) {
        try {
            WorldGuard.getInstance().getFlagRegistry().register(flag);
            return flag;
        } catch (FlagConflictException | IllegalStateException e) {
            return (T) WorldGuard.getInstance().getFlagRegistry().get(flag.getName());
        }
    }

    @Override
    public void onEnable() {
        WorldEditPlugin worldEdit = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        weSchemDir = new File(worldEdit.getDataFolder(), "schematics");
        if (!weSchemDir.exists()) {
            weSchemDir.mkdirs();
        }
        loadConfig();
        getCommand("plotgenerator").setExecutor(new PlotGeneratorCommand(this));
        getCommand("buyplot").setExecutor(new BuyPlotCommand(this));
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        worldConfigs = new HashMap<>();
        getConfig().getConfigurationSection("worlds").getKeys(false).forEach(this::getGeneratorConfig);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new PlotChunkGenerator(this, id);
    }

    public WorldGuardPlugin getWorldGuard() {
        if (worldGuard == null && getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuard = WorldGuardPlugin.inst();
        }
        return worldGuard;
    }

    public PlotSigns getPlotSigns() {
        if (plotSigns == null && getServer().getPluginManager().isPluginEnabled("PlotSigns")) {
            plotSigns = PlotSigns.getPlugin(PlotSigns.class);
        }
        return plotSigns;
    }

    public Economy getEconomy() {
        if (economy == null) {
            if (getServer().getPluginManager().isPluginEnabled("Vault")) {
                return null;
            }
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return null;
            }
            economy = rsp.getProvider();
        }
        return economy;
    }

    public CompletableFuture<PlotSchematic> loadSchematic(String schematicName) {
        return CompletableFuture.supplyAsync(() -> {
            if (schematicName == null || schematicName.isEmpty()) {
                return null;
            }

            File file = new File(getDataFolder(), schematicName + ".schem");
            if (!file.exists()){
                file = new File(weSchemDir, schematicName + ".schem");
            }
            if (!file.exists()){
                file = new File(getDataFolder(), schematicName + ".schematic");
            }
            if (!file.exists()){
                file = new File(weSchemDir, schematicName + ".schematic");
            }
            if (!file.exists()) {
                getLogger().log(Level.SEVERE, "No schematic found with the name " + schematicName + "!");
                return null;
            }

            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                getLogger().log(Level.SEVERE, "Could not load schematic format from file " + file.getAbsolutePath() + "!");
                return null;
            }

            try {
                return new PlotSchematic(loadSchematicFromFile(file, format));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error loading file " + file.getAbsolutePath(), e);
                return null;
            }
        });
    }

    private Clipboard loadSchematicFromFile(File file, ClipboardFormat format) throws IOException {
        try (Closer closer = Closer.create()) {
            FileInputStream fis = closer.register(new FileInputStream(file));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = closer.register(format.getReader(bis));

            return reader.read();
        }
    }

    public PlotGeneratorConfig getGeneratorConfig(World world) {
        return getGeneratorConfig(world.getName());
    }

    public PlotGeneratorConfig getGeneratorConfig(String worldName) {
        if (worldConfigs.containsKey(worldName)) {
            return worldConfigs.get(worldName);
        }
        ConfigurationSection config = getConfig().getConfigurationSection("worlds." + worldName);
        PlotGeneratorConfig info = PlotGeneratorConfig.fromConfig(this, config);
        if (info != null) {
            worldConfigs.put(worldName, info);
        }
        return info;
    }

    public void registerRegionIntent(RegionIntent intent) {
        if (regionCreatorTask == -1) {
            regionCreatorTask = scheduleRegionCreator();
        }
        regionIntents.put(intent, true);
    }

    private int scheduleRegionCreator() {
        return getServer().getScheduler().runTask(this, () -> {
            Iterator<Map.Entry<RegionIntent, Boolean>> intents = regionIntents.entrySet().iterator();
            while (intents.hasNext()) {
                RegionIntent intent = intents.next().getKey();
                intents.remove();
                if (getWorldGuard() != null) {
                    RegionManager manager = getRegionManager(intent.getWorld());
                    if (testForRegion(intent)) {
                        if (intent.getSign() != null) {
                            registerBuySign(intent);
                        }
                        continue;
                    }
                    String regionId = getNewRegionId(intent);
                    ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, intent.getMinPoint(), intent.getMaxPoint());

                    double tpX = region.getMinimumPoint().getX() + (region.getMaximumPoint().getX() - region.getMinimumPoint().getX()) / 2;
                    double tpZ = region.getMaximumPoint().getZ();
                    double tpY = intent.getWorld().getHighestBlockYAt((int) tpX, (int) tpZ) + 1;
                    region.setFlag(Flags.TELE_LOC, new Location(new BukkitWorld(intent.getWorld()), Vector3.at(tpX, tpY, tpZ), 180, 0));
                    if (intent.getConfig().getRegionPrice() > 0) {
                        region.setFlag(BUYABLE_FLAG, true);
                        region.setFlag(PRICE_FLAG, intent.getConfig().getRegionPrice());
                    }
                    if (intent.getConfig().getPlotType() != null && !intent.getConfig().getPlotType().isEmpty() && getPlotSigns() != null) {
                        region.setFlag(PlotSigns.PLOT_TYPE_FLAG, intent.getConfig().getPlotType());
                    }

                    manager.addRegion(region);
                    getLogger().log(Level.INFO, "Added new region " + regionId + " at " + intent.getMinPoint() + " " + intent.getMaxPoint());
                    if (intent.getSign() != null) {
                        registerBuySign(intent);
                    }
                }
            }
            regionCreatorTask = -1;
        }).getTaskId();
    }

    private void registerBuySign(RegionIntent intent) {
        if (getWorldGuard() == null || getPlotSigns() == null || intent.getConfig().getRegionPrice() < 0) {
            return;
        }

        ProtectedRegion region = getSimilarRegion(intent, intent.getSign());

        if (region == null) {
            getLogger().log(Level.WARNING, "Sign was found at " + intent.getSign() + " but no region?");
            return;
        }

        Block block = intent.getWorld().getBlockAt(intent.getSign().getBlockX(), intent.getSign().getBlockY(), intent.getSign().getBlockZ());
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            try {
                String[] lines = getPlotSigns().getSignLines(region);
                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                }
                sign.getPersistentDataContainer().set(PlotSigns.SIGN_REGION_KEY, PersistentDataType.STRING, region.getId());
                sign.update();
                getLogger().log(Level.INFO, "Wrote PlotSigns sign for region " + intent.getWorld().getName() + "/" + region.getId() + " at " + intent.getSign());
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE, "Could not create PlotSigns sign! ", e);
            }
        }
    }

    /**
     * Get a new region id that hasn't been registered with worldguard yet
     * @return
     */
    private String getNewRegionId(RegionIntent intent) {
        RegionManager manager = getRegionManager(intent.getWorld());
        String mapKey = intent.getWorld().getName() + "_" + intent.getConfig().getRegionId();
        int idNumber = 0;
        if (regionIds.containsKey(mapKey)) {
            idNumber = regionIds.get(mapKey);
        }

        String regionName;
        do {
            idNumber++;
            regionName = intent.getConfig().getRegionId().replace("%world%", intent.getWorld().getName());
            regionName = regionName.contains("%number%") ? regionName.replace("%number%", String.valueOf(idNumber)) : regionName + idNumber;
        } while (manager.getRegion(regionName) != null);

        regionIds.put(mapKey, idNumber);
        return regionName;
    }

    /**
     * Test whether or not there already is a similar region
     * @param intent
     * @return
     */
    private boolean testForRegion(RegionIntent intent) {
        return getSimilarRegion(intent, intent.getMinPoint()) != null || getSimilarRegion(intent, intent.getMaxPoint()) != null;
    }

    /**
     * Get a region at that location that is similar to the intent
     * @param intent
     * @return
     */
    private ProtectedRegion getSimilarRegion(RegionIntent intent, BlockVector3 loc) {
        String regionRegexString = intent.getConfig().getRegionId().replace("%world%", "\\w+");
        regionRegexString = regionRegexString.contains("%number%") ? regionRegexString.replace("%number%", "\\d+") : regionRegexString + "\\d+";
        Pattern regionRegex = Pattern.compile("^" + regionRegexString + "$");
        RegionManager manager = getRegionManager(intent.getWorld());
        ApplicableRegionSet regions = manager.getApplicableRegions(loc);
        for (ProtectedRegion region : regions) {
            if (regionRegex.matcher(region.getId()).matches()) {
                return region;
            }
        }
        return null;
    }

    public static RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world));
    }
}
