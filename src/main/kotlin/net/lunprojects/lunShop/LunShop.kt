package net.lunprojects.lunShop

import net.lunprojects.lunShop.commands.AdminEconomyHandler
import net.lunprojects.lunShop.commands.PayCommand
import net.lunprojects.lunShop.events.PlayerDataHandler
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class LunShop : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        logger.info("LunShop has been enabled successfully!")
        saveDefaultConfig()

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

        // TEST THIS: Add the ability to pay other players with your own money.
        val payCommand = PayCommand(this)
        getCommand("pay")?.setExecutor(payCommand)

        // TODO: Add the ability to see your current balance with a scoreboard thing.
        // TODO: Add Page support when the ShopItems become to big (In Size) so the player can click to the next page.


    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("LunShop is shutting down.")
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
}
