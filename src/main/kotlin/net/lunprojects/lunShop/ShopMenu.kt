package net.lunprojects.lunShop

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class ShopMenu(val items: List<ShopItem>, val page: Int = 0) : InventoryHolder {
    private val inv: Inventory = Bukkit.createInventory(this, 27, Component.text("LunShop Menu"))

    data class ShopItem (
        val name: String,
        val buyPrice: Int,
        val sellPrice: Int
    )

    init {
        val start = page * 18
        val end = minOf(start + 18, items.size)

        // Fill item slots
        for (i in start until end) {
            inv.setItem(i-start, createGuiItem(items[i]))
        }

        // Add Naigation Buttons
        if (page > 0) {
            inv.setItem(18, createNavigationItem("§eBack", Material.ARROW))
        }

        if (end < items.size) {
            inv.setItem(26, createNavigationItem("§eNext", Material.ARROW))
        }
    }

    private fun createNavigationItem(name: String, material: Material): ItemStack {
        val stack = ItemStack(material)
        val meta = stack.itemMeta
        meta.displayName(Component.text(name))
        stack.itemMeta = meta
        return stack
    }

    private fun createGuiItem(item: ShopItem): ItemStack {
        val material = Material.matchMaterial(item.name) ?: Material.BARRIER
        val stack = ItemStack(material)
        val meta = stack.itemMeta

        meta.displayName(Component.text(item.name))
        meta.lore(listOf(
            Component.text("§aLeft-Click to Buy: $${item.buyPrice}"),
            Component.text("§cRight-Click to Sell: $${item.sellPrice}")
        ))
        stack.itemMeta = meta
        return stack
    }

    override fun getInventory(): Inventory = inv

}