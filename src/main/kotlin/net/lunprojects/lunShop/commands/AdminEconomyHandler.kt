package net.lunprojects.lunShop.commands

import net.lunprojects.lunShop.LunShop
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType

class AdminEconomyHandler(private val plugin: LunShop) : CommandExecutor {

    private val moneyKey = NamespacedKey(plugin, "player_balance")

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        // 1. Check if the sender is an OP (Operator)
        if (!sender.isOp) {
            sender.sendMessage("§cYou do not have permission to use this command!")
            return true
        }

        // 2. Validate arguments: /addmoney <player> <amount>
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /addmoney <player> <amount>")
            return true
        }

        val target = Bukkit.getPlayer(args[0])
        val amount = args[1].toDoubleOrNull()

        if (target == null) {
            sender.sendMessage("§cPlayer not found!")
            return true
        }

        if (amount == null || amount <= 0) {
            sender.sendMessage("§cPlease provide a valid positive number.")
            return true
        }

        // 3. Update the target player's PDC balance
        val container = target.persistentDataContainer
        val currentBalance = container.getOrDefault(moneyKey, PersistentDataType.DOUBLE, 0.0)
        val newBalance = currentBalance + amount

        container.set(moneyKey, PersistentDataType.DOUBLE, newBalance)

        // 4. Send confirmation messages
        sender.sendMessage("§aAdded $$amount to ${target.name}'s balance. New total: $$newBalance")
        target.sendMessage("§aAn admin added $$amount to your balance! New total: $$newBalance")

        return true
    }
}