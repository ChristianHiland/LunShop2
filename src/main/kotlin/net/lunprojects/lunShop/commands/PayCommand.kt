package net.lunprojects.lunShop.commands

import net.lunprojects.lunShop.LunShop
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PayCommand(private val plugin: LunShop) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // 2. Validate arguments: /pay <player> <amount>
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /pay <player> <amount>")
            return true
        }

        val target: Player = Bukkit.getPlayer(args[0]) ?: return true
        val amount: Double = args[1].toDouble()
        val player: Player = sender as? Player ?: return true

        val senderBal = plugin.getBalance(player)
        if (senderBal - amount < 0) {
            sender.sendMessage("§cYou don't have enough money to pay that amount")
            return true
        }

        val senderNewBal = senderBal - amount
        val targetBal = plugin.getBalance(target)
        plugin.setBalance(target, targetBal + amount)
        return true
    }
}