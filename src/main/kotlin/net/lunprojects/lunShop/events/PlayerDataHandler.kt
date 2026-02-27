package net.lunprojects.lunShop.events

import net.lunprojects.lunShop.LunShop
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType

class PlayerDataHandler(private val plugin: LunShop) : Listener {

    // Create a unique key for the money data
    private val moneyKey = NamespacedKey(plugin, "player_balance")
    private val defaultBalance = 100000.0 // Set your starting amount here

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val container = player.persistentDataContainer

        // Check if the player already has a balance set
        if (!container.has(moneyKey, PersistentDataType.DOUBLE)) {
            // New player detected: initialize with default amount
            container.set(moneyKey, PersistentDataType.DOUBLE, defaultBalance)

            player.sendMessage("§aWelcome! You've been given a starting balance of $$defaultBalance.")
            plugin.logger.info("Initialized starting balance for new player: ${player.name}")
        }
    }


}