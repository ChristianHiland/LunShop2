package net.lunprojects.lunShop

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ShopHandler(val shopItems: List<ShopMenu.ShopItem>, val plugin: LunShop) : CommandExecutor, Listener {
    // Part A: Open the GUI when the command is run
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            sender.openInventory(ShopMenu(shopItems).inventory)
        }
        return true
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        // Check if the inventory being clicked is the LunShop
        val holder = event.inventory.holder as? ShopMenu ?: return
        // Cancel the event so the player can't take the item
        event.isCancelled = true

        val slot = event.slot
        val shopItem = holder.items.getOrNull(slot) ?: return
        val clickedItem = event.currentItem ?: return
        val player = event.whoClicked as Player

        // Perform an action based on the item clicked
        if (clickedItem.type != Material.BARRIER) {
            if (event.isLeftClick) {
                handleBuy(player, shopItem)
            } else if (event.isRightClick) {
                handleSell(player, shopItem)
            }
        }
    }

    private fun handleBuy(player: org.bukkit.entity.Player, item: ShopMenu.ShopItem) {
        val key = NamespacedKey(plugin, "player_balance")
        val currentBalance = player.persistentDataContainer.getOrDefault(key, PersistentDataType.DOUBLE, 0.0)

        // Check if player has enough money
        if (currentBalance >= item.buyPrice) {
            val newBalance = currentBalance - item.buyPrice
            player.persistentDataContainer.set(key, PersistentDataType.DOUBLE, newBalance)

            // Give the item to the player
            val material = Material.matchMaterial(item.name) ?: return
            player.inventory.addItem(ItemStack(material, 1))

            player.sendMessage("§aBought ${item.name} for $${item.buyPrice}! New balance: $$newBalance")
        } else {
            player.sendMessage("§cYou don't have enough money! You need $${item.buyPrice - currentBalance} more.")
        }
    }

    public fun handleSell(player: org.bukkit.entity.Player, item: ShopMenu.ShopItem) {
        val material = Material.matchMaterial(item.name) ?: return

        // Check if the player actually has the item
        if (player.inventory.contains(material)) {
            player.inventory.removeItem(ItemStack(material, 1))

            val key = NamespacedKey(plugin, "player_balance")
            val currentBalance = player.persistentDataContainer.getOrDefault(key, PersistentDataType.DOUBLE, 0.0)
            val newBalance = currentBalance + item.sellPrice

            // Update the PDC balance
            player.persistentDataContainer.set(key, PersistentDataType.DOUBLE, newBalance)

            player.sendMessage("§eSold ${item.name} for $${item.sellPrice}! New balance: $$newBalance")
        } else {
            player.sendMessage("§cYou don't have any ${item.name} to sell!")
        }
    }
}