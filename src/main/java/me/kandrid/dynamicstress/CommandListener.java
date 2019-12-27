package me.kandrid.dynamicstress;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player && sender.isOp()) {
            Player player = (Player) sender;

            if (command.getName().equals("ds_debug")) {
                DynamicStress.debug = !DynamicStress.debug;
                if (DynamicStress.debug) {
                    player.sendRawMessage(ChatColor.RED + "DynamicStress Debug Mode Activated");
                } else {
                    player.sendRawMessage(ChatColor.GREEN + "DynamicStress Debug Mode Deactivated");
                }
                return true;
            }
        }

        return false;
    }

}
