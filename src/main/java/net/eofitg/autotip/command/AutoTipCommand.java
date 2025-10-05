package net.eofitg.autotip.command;

import net.eofitg.autotip.AutoTip;
import net.eofitg.autotip.util.PlayerUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoTipCommand extends CommandBase {

    private static final List<String> SUBCOMMANDS = Arrays.asList("toggle", "interval");

    @Override
    public String getCommandName() {
        return "autotip";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("at", "atm", "autotipmod");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/autotip toggle|interval <value>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) return;
        if (args.length == 0) {
            PlayerUtil.addMessage(EnumChatFormatting.YELLOW + "Usage: " + getCommandUsage(sender));
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "toggle": {
                AutoTip.config.enabled = !AutoTip.config.enabled;
                boolean isEnabled = AutoTip.config.enabled;
                String status = isEnabled ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled";
                PlayerUtil.addMessage(EnumChatFormatting.GOLD + "Mod " + status + EnumChatFormatting.GOLD + ".");
                AutoTip.saveConfig();
                break;
            }
            case "interval": {
                if (args.length >= 2) {
                    try {
                        AutoTip.config.interval = Integer.parseInt(args[1]);
                        PlayerUtil.addMessage(EnumChatFormatting.GOLD + "Set tip interval time to " + EnumChatFormatting.ITALIC + EnumChatFormatting.WHITE + AutoTip.config.interval + "s" + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + ".");
                        AutoTip.saveConfig();
                    } catch (NumberFormatException e) {
                        PlayerUtil.addMessage(EnumChatFormatting.RED + "Please input a valid number!");
                    }
                } else {
                    PlayerUtil.addMessage(EnumChatFormatting.GOLD + "Current tip interval time is " + EnumChatFormatting.ITALIC + EnumChatFormatting.WHITE + AutoTip.config.interval + "s" + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + ".");
                }
                break;
            }
            default: {
                PlayerUtil.addMessage(EnumChatFormatting.RED + "Unknown argument: " + sub);
                PlayerUtil.addMessage(EnumChatFormatting.YELLOW + "Usage: " + getCommandUsage(sender));
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String cmd : SUBCOMMANDS) {
                if (cmd.startsWith(prefix)) {
                    completions.add(cmd);
                }
            }
        }
        return completions.isEmpty() ? null : completions;
    }

}
