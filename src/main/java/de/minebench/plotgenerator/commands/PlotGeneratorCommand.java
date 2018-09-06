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

import de.minebench.plotgenerator.PlotGenerator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class PlotGeneratorCommand implements CommandExecutor {
    private final PlotGenerator plugin;

    public PlotGeneratorCommand(PlotGenerator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0])) {
                String cmdPerm = command.getPermission();
                command.setPermission(cmdPerm + ".reload");
                if (command.testPermission(sender)) {
                    plugin.loadConfig();
                    sender.sendMessage(ChatColor.GREEN + plugin.getName() + " config reloaded!");
                }
                command.setPermission(cmdPerm);
                return true;
            } else if ("buy".equalsIgnoreCase(args[0])) {
                String cmdPerm = command.getPermission();
                command.setPermission(cmdPerm + ".buy");
                if (command.testPermission(sender)) {
                    new BuyPlotCommand(plugin).onCommand(sender, plugin.getCommand("buyplot"), "buyplot", Arrays.copyOfRange(args, 1, args.length));
                }
                command.setPermission(cmdPerm);
            }
        }
        return false;
    }
}
