package de.minebench.plotgenerator.commands;

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
