package net.lunprojects.lunShop.commands

import net.lunprojects.lunShop.LunShop
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BalanceCommand(private val plugin: LunShop) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // 2. Validate arguments: /pay <player> <amount>
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /balance")
            return true
        }

        val player: Player = sender as? Player ?: return true

        val targetBal = plugin.getBalance(player)
        player.sendMessage("You have a new balance of $$targetBal")
        return true
    }
}