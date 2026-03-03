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
            (state as? org.bukkit.block.TileState)?.persistentDataContainer?.set(chestKey, PersistentDataType.BYTE, 1.toByte())
            state.update()
            event.player.sendMessage("§aAuto-Sell Chest placed!")
            startAutoSellTask()
        }
    }

    fun startAutoSellTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                val block = player.location.block
                val state = block.state as? org.bukkit.block.Chest ?: continue

                if (state.persistentDataContainer.has(chestKey, PersistentDataType.BYTE)) {
                    processSell(state, player)
                }
            }
        }, 1200L, 1200L)
    }

    private fun processSell(chest: org.bukkit.block.Chest, owner: Player) {
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