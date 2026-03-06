package net.lunprojects.lunShop

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType

class AutoSellChestHandler(private val plugin: LunShop, private val shopItems: List<ShopMenu.ShopItem>) : CommandExecutor, Listener {
    private val chestKey = NamespacedKey(plugin, "is_auto_sell_chest")
    private val playerKey = NamespacedKey(plugin, "owner")

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val player: Player = sender
            giveAutoSellChest(player)
        }
        return true
    }

    fun giveAutoSellChest(player: Player) {
        val chest = ItemStack(Material.CHEST)
        val meta = chest.itemMeta

        meta.displayName(Component.text("§6§lAuto-Sell Chest"))
        meta.lore(listOf(Component.text("§7Automatically sells contents every 60 seconds.")))

        // Tag the item
        meta.persistentDataContainer.set(chestKey, PersistentDataType.BYTE, 1.toByte())

        chest.itemMeta = meta
        player.inventory.addItem(chest)
    }

    // Handle The Player Placing the chest
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (item.itemMeta?.persistentDataContainer?.has(chestKey, PersistentDataType.BYTE) == true) {
            val state = event.blockPlaced.state
            val block = event.blockPlaced

            // Setting chestKey, and Player name key
            (state as? org.bukkit.block.TileState)?.persistentDataContainer?.set(chestKey, PersistentDataType.BYTE, 1.toByte())
            (state as? org.bukkit.block.TileState)?.persistentDataContainer?.set(playerKey, PersistentDataType.STRING, event.player.uniqueId.toString())
            state.update()
            plugin.autoSellChests.add(block.location)
            event.player.sendMessage("§aAuto-Sell Chest placed & Linked!")
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockPlaceEvent) {
        if (plugin.autoSellChests.contains(event.block.location)) {
            plugin.autoSellChests.remove(event.block.location)
            event.player.sendMessage("§eAuto-Sell Chest removed & Delinked.")
        }
    }

    public fun processSell(chest: org.bukkit.block.Chest, owner: Player) {
        var totalProfit = 0.0
        val inventory = chest.inventory

        for (item in inventory.contents) {
            if (item == null) continue

            // Match the item to Shop Items from the config
            val shopItem = shopItems.find { it.name == item.type.name } ?: continue

            totalProfit += shopItem.sellPrice * item.amount
            inventory.remove(item)
        }

        if (totalProfit > 0) {
            plugin.setBalance(owner, plugin.getBalance(owner) + totalProfit)
            owner.sendMessage("§6[Auto-Sell] §aSold chest contents for $$totalProfit!")
        }
    }

}