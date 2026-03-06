package net.lunprojects.lunShop

import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import net.lunprojects.lunShop.menus.TransactionMenu
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
        val holder = event.inventory.holder
        // Cancel the event so the player can't take the item
        event.isCancelled = true

        val slot = event.slot
        val clickedItem = event.currentItem ?: return
        val player = event.whoClicked as Player

        // 1. Check if the player is in the MAIN SHOP
        val shopHolder = event.inventory.holder as? ShopMenu
        if (shopHolder != null) {
            event.isCancelled = true // Prevent taking items

            // Handle Page Navigation
            if (slot == 18 && clickedItem.type == Material.ARROW) {
                player.openInventory(ShopMenu(shopHolder.items, shopHolder.page - 1).inventory)
                return
            }
            if (slot == 26 && clickedItem.type == Material.ARROW) {
                player.openInventory(ShopMenu(shopHolder.items, shopHolder.page + 1).inventory)
                return
            }

            // Open Transaction Menu for items in slots 0-17
            if (slot < 18 && clickedItem.type != Material.BARRIER) {
                val itemIndex = slot + (shopHolder.page * 18)
                val shopItem = shopHolder.items.getOrNull(itemIndex) ?: return
                player.openInventory(TransactionMenu(shopItem).inventory)
            }
            return // Stop processing so we don't hit the next check
        }

        // Handling The Transaction Menu Events
        val transHolder = event.inventory.holder as? TransactionMenu
        if (transHolder != null) {
            event.isCancelled = true
            handleTransactionClick(event, transHolder, player)
        }
    }

    private fun handleTransactionClick(event: InventoryClickEvent, holder: TransactionMenu, player: Player) {
        when (event.slot) {
            11 -> { // Subtract
                if (holder.amount > 1) {
                    plugin.logger.info("Dec Item Amount")
                    player.openInventory(TransactionMenu(holder.item, holder.amount - 1).inventory)
                }
            }
            15 -> { // Add
                if (holder.amount < 64) {
                    plugin.logger.info("Inc Item Amount")
                    player.openInventory(TransactionMenu(holder.item, holder.amount + 1).inventory)
                }
            }
            22 -> { // Confirm
                // Scale your existing handleBuy logic by holder.amount
                if (event.isLeftClick) executeBulkBuy(player, holder.item, holder.amount)
                if (event.isRightClick) executeBulkSell(player, holder.item, holder.amount)

                player.closeInventory()
            }
        }
    }

    private fun executeBulkBuy(player: Player, item: ShopMenu.ShopItem, amount: Int) {
        val key = NamespacedKey(plugin, "player_balance")
        val currentBalance = player.persistentDataContainer.getOrDefault(key, PersistentDataType.DOUBLE, 0.0)
        val buyTotalPrice = item.buyPrice * amount
        plugin.logger.info("Execute Bulk Buy")

        if (currentBalance >= buyTotalPrice) {
            // Setting player's new balance
            val newBalance = currentBalance - buyTotalPrice
            player.persistentDataContainer.set(key, PersistentDataType.DOUBLE, newBalance)

            // Giving the item to the player
            val material = Material.matchMaterial(item.name) ?: return
            player.inventory.addItem(ItemStack(material, amount))
            player.sendMessage("§aBought $amount ${item.name} for $${buyTotalPrice}! New balance: $$newBalance")
        } else {
            player.sendMessage("§cYou don't have enough money! You need $${buyTotalPrice - currentBalance} more.")
        }
    }

    private fun executeBulkSell(player: Player, item: ShopMenu.ShopItem, amount: Int) {
        val key = NamespacedKey(plugin, "player_balance")
        val material = Material.matchMaterial(item.name) ?: return
        val currentBalance = player.persistentDataContainer.getOrDefault(key, PersistentDataType.DOUBLE, 0.0)
        val totalSellPrice = item.sellPrice * amount
        plugin.logger.info("Execute Bulk Sell")

        if (player.inventory.contains(material)) {
            player.inventory.removeItem(ItemStack(material, amount))
            val newBalance = currentBalance + totalSellPrice

            // Update PDC Balance
            player.persistentDataContainer.set(key, PersistentDataType.DOUBLE, newBalance)
            player.sendMessage("§eSold $amount ${item.name} for $${totalSellPrice}! New balance: $$newBalance")
        } else {
            player.sendMessage("§cYou don't have any ${item.name} to sell!")
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