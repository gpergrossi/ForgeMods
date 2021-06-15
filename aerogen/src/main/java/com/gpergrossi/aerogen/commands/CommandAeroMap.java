package com.gpergrossi.aerogen.commands;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.AeroGenerator;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandAeroMap implements ICommand {

	@Override
	public int compareTo(ICommand arg0) {
		return 0;
	}

	@Override
	public String getName() {
		return "aeromap";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/aeromap";
	}

	@Override
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add("aeromap");
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) AeroGenerator.toggleGUI();
		if (args.length == 1) {
			if (args[0].equals("show") || args[0].equals("on")) {
				AeroGenerator.setGUIEnabled(true);
				return;
			}
			if (args[0].equals("hide") || args[0].equals("off")) {
				AeroGenerator.setGUIEnabled(false);
				return;
			}
			
			try {
				int dimension = Integer.parseInt(args[0]);
				AeroGenerator.showGUIDimension(dimension);
			} catch (NumberFormatException e) {}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return new ArrayList<>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

}
