package net.lunprojects.lunShop

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class ShopMenu(val items: List<ShopItem>) : InventoryHolder {
    private val inv: Inventory = Bukkit.createInventory(this, 27, Component.text("LunShop Menu"))

    data class ShopItem (
        val name: String,
        val buyPrice: Int,
        val sellPrice: Int
    )

    init {
        items.forEachIndexed { index, shopItem ->
            if (index < 27) {
                inv.setItem(index, createGuiItem(shopItem))
            }
        }
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