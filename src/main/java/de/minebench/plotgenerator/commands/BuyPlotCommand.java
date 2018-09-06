package de.minebench.plotgenerator.commands;

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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.minebench.plotgenerator.PlotGenerator;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.minebench.plotgenerator.PlotGenerator.BUYABLE_FLAG;
import static de.minebench.plotgenerator.PlotGenerator.PRICE_FLAG;

public class BuyPlotCommand implements CommandExecutor {
    private final PlotGenerator plugin;

    public BuyPlotCommand(PlotGenerator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
            return true;
        }

        if (plugin.getWorldGuard() == null) {
            sender.sendMessage(ChatColor.RED + "WorldGuard is not installed!");
            return true;
        }

        if (plugin.getEconomy() == null) {
            sender.sendMessage(ChatColor.RED + "Vault is not installed!");
            return true;
        }

        Player player = (Player) sender;

        ProtectedRegion region = null;

        if (args.length > 0) {
            String cmdPerm = command.getPermission();
            command.setPermission(cmdPerm + ".byregionid");
            if (command.testPermission(sender)) {
                ProtectedRegion r = PlotGenerator.getRegionManager(player.getWorld()).getRegion(args[0]);
                if (r == null) {
                    sender.sendMessage(ChatColor.RED + "No region found with the id " + ChatColor.YELLOW + args[0]);
                } else if (r.getFlag(BUYABLE_FLAG) != null && r.getFlag(BUYABLE_FLAG) && r.getFlag(PRICE_FLAG) != null && r.getFlag(PRICE_FLAG) > 0) {
                    region = r;
                } else {
                    sender.sendMessage(ChatColor.RED + "The region " + ChatColor.YELLOW + args[0] + ChatColor.RED + " is not buyable!");
                }
            }
            command.setPermission(cmdPerm);
        } else {
            Location l = player.getLocation();
            ApplicableRegionSet regions = PlotGenerator.getRegionManager(player.getWorld()).getApplicableRegions(new Vector(l.getX(), l.getY(), l.getZ()));
            for (ProtectedRegion r : regions.getRegions()) {
                if (r.getFlag(BUYABLE_FLAG) != null && r.getFlag(BUYABLE_FLAG) && r.getFlag(PRICE_FLAG) != null && r.getFlag(PRICE_FLAG) > 0) {
                    region = r;
                }
            }
            if (region == null) {
                sender.sendMessage(ChatColor.RED + "No buyable region found at your current location!");
            }
        }

        if (region == null) {
            return true;
        }

        double price = region.getFlag(PRICE_FLAG);
        EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, price);
        if (response.transactionSuccess()) {
            region.setFlag(BUYABLE_FLAG, false);
            region.getOwners().clear();
            region.getOwners().addPlayer(player.getUniqueId());
            region.getMembers().clear();
            sender.sendMessage(ChatColor.GREEN + "You bought the plot " + region.getId() + " for " + price + "!");
        } else {
            sender.sendMessage(ChatColor.RED + "Cannot buy plot! " + response.errorMessage);
        }
        return true;
    }
}
