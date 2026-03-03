package net.lunprojects.lunShop

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Criteria
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.DisplaySlot

class LeaderBoard(var plugin: LunShop) : Listener {

    // Update the Leaderboard for a player.
    public fun updateScoreboard(player: Player) {
        val manager = Bukkit.getScoreboardManager()
        val scoreboard = manager.newScoreboard

        // Create the objective (the sidebar itself)
        val title = LegacyComponentSerializer.legacySection().deserialize("§6§lLunbin's Server")
        val objective = scoreboard.registerNewObjective("balance_board", Criteria.DUMMY, title)
        objective.displaySlot = DisplaySlot.SIDEBAR

        // Get the player's balance from PDC
        val balance = plugin.getBalance(player)

        // Set the lines (Scores). Lower numbers are at the bottom
        objective.getScore("§7--- Stats ---").score = 4
        objective.getScore("§fBalance: §a$$balance").score = 3
        objective.getScore("§7--------------").score = 2
        objective.getScore("Website: lunprojects.net").score = 1

        player.scoreboard = scoreboard
    }

    public fun updateLeaderboard() {
        val topPlayers = Bukkit.getOnlinePlayers()
            .sortedByDescending { plugin.getBalance(it) } // Sort by richest
            .take(5) // Get the top 5

        for (player in Bukkit.getOnlinePlayers()) {
            val board = player.scoreboard
            val obj = board.getObjective("balance_board") ?: continue

            // Remove old scores and set new top 5 lines
            topPlayers.forEachIndexed { index, topPlayer ->
                obj.getScore("§e${index + 1}. ${topPlayer.name}: §a$${plugin.getBalance(topPlayer)}").score = 10 - index
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        updateScoreboard(player)
    }
}