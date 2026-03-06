package net.lunprojects.lunShop

import net.lunprojects.lunShop.commands.AdminEconomyHandler
import net.lunprojects.lunShop.commands.BalanceCommand
import net.lunprojects.lunShop.commands.PayCommand
import net.lunprojects.lunShop.events.PlayerDataHandler
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

class LunShop : JavaPlugin() {

    private val chestKey = NamespacedKey(this, "is_auto_sell_chest")
    private val playerKey = NamespacedKey(this, "owner")
    val autoSellChests = mutableListOf<org.bukkit.Location>()

    override fun onEnable() {
        // Plugin startup logic
        logger.info("LunShop has been enabled successfully!")
        saveDefaultConfig()
        loadChests()

        // Loading Shop Items
        logger.info("Loading Shop Items.")
        val shopItems = loadShopItems()
        val shopItemsSize = shopItems.size
        logger.info("Loaded $shopItemsSize items")

        // Setting up The LunShop Menu
        val handler = ShopHandler(shopItems, this)
        getCommand("shop")?.setExecutor(handler)
        server.pluginManager.registerEvents(handler, this)

        // Setting up Player Data Handler
        server.pluginManager.registerEvents(PlayerDataHandler(this), this)
        // Setting up Admin Economy Handler
        getCommand("addMoney")?.setExecutor(AdminEconomyHandler(this))

        // Setting up Auto Sell Chest Handler
        val autoSellHandler = AutoSellChestHandler(this, shopItems)
        getCommand("shopChest")?.setExecutor(autoSellHandler)
        server.pluginManager.registerEvents(autoSellHandler, this)
        // Timer loop for auto sell chests
        Bukkit.getScheduler().runTaskTimer(this, Runnable {
            val iterator = autoSellChests.iterator()

            while (iterator.hasNext()) {
                val loc = iterator.next()
                val block = loc.block

                // Check if the block is still a chest with the correct keys
                val state = block.state as? org.bukkit.block.Chest
                if (state == null || !state.persistentDataContainer.has(chestKey, PersistentDataType.BYTE)) {
                    iterator.remove()
                    continue
                }

                val ownerUUID = state.persistentDataContainer.get(playerKey, PersistentDataType.STRING)
                val owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID))

                autoSellHandler.processSell(state, owner as Player)
            }
        }, 0L, 500L) // Updates every 5 seconds (100 ticks)

        // TEST THIS
        // Setting up Balance Command
        val balanceCommand = BalanceCommand(this)
        getCommand("balance")?.setExecutor(autoSellHandler)
        server.pluginManager.registerEvents(autoSellHandler, this)

        // TEST THIS: Add the ability to pay other players with your own money.
        val payCommand = PayCommand(this)
        getCommand("pay")?.setExecutor(payCommand)

        // TEST THIS
        var leaderBoard = LeaderBoard(this)
        server.pluginManager.registerEvents(leaderBoard, this)
        Bukkit.getScheduler().runTaskTimer(this, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                leaderBoard.updateScoreboard(player)
            }
        }, 0L, 100L) // Updates every 5 seconds (100 ticks)
        // TODO: Add Page support when the ShopItems become to big (In Size) so the player can click to the next page.
        // TODO: Add balance command


    }
    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("LunShop is shutting down. Saving live vars...")
        saveChests()
    }

    private fun loadShopItems(): List<ShopMenu.ShopItem> {
        val shopList = mutableListOf<ShopMenu.ShopItem>()
        val configItems = config.getMapList("shop-items")

        for (itemMap in configItems) {
            val name = itemMap["name"] as? String ?: "Unknown"
            val buy = itemMap["buy-price"] as? Int ?: 0
            val sell = itemMap["sell-price"] as? Int ?: 0
            shopList.add(ShopMenu.ShopItem(name, buy, sell))
        }
        return shopList
    }

    fun getBalance(player: Player): Double {
        val key = NamespacedKey(this, "player_balance")
        return player.persistentDataContainer.getOrDefault(key, PersistentDataType.DOUBLE, 0.0)
    }

    fun setBalance(player: Player, amount: Double) {
        val key = NamespacedKey(this, "player_balance")
        player.sendMessage("You have a new balance of $$amount")
        player.persistentDataContainer.set(key, PersistentDataType.DOUBLE, amount)
    }

    private fun saveChests() {
        val file = File(dataFolder, "chests.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        // Convert Locations to Strings (world,x,y,z) for easy storage
        val serializedChests = autoSellChests.map { loc ->
            "${loc.world?.name},${loc.blockX},${loc.blockY},${loc.blockZ}"
        }

        config.set("chests", serializedChests)
        config.save(file)
    }

    private fun loadChests() {
        val file = File(dataFolder, "chests.yml")
        if (!file.exists()) return

        val config = YamlConfiguration.loadConfiguration(file)
        val serializedChests = config.getStringList("chests")

        for (str in serializedChests) {
            val parts = str.split(",")
            if (parts.size == 4) {
                val world = Bukkit.getWorld(parts[0])
                val x = parts[1].toInt()
                val y = parts[2].toInt()
                val z = parts[3].toInt()

                if (world != null) {
                    autoSellChests.add(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
                }
            }
        }
    }
}
