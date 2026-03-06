package net.lunprojects.lunShop.menus

import net.kyori.adventure.text.Component
import net.lunprojects.lunShop.ShopMenu
import net.lunprojects.lunShop.ShopMenu.ShopItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class TransactionMenu(val item: ShopMenu.ShopItem, var amount: Int = 1) : InventoryHolder {
    private val inv = Bukkit.createInventory(this, 27, Component.text("Buying: ${item.name}"))

    init {
        // Set Slot 4 the item being traded
        inv.setItem(4, createGuiItem(item, amount))

        // Slots 11 & 15: Adjust Quantity
        inv.setItem(11, createButtonItem("§c-1", Material.RED_STAINED_GLASS_PANE))
        inv.setItem(13, createButtonItem("§eQuantity: $amount", Material.PAPER))
        inv.setItem(15, createButtonItem("§a+1", Material.GREEN_STAINED_GLASS_PANE))

        // Slot 22: Confirm Purchase
        inv.setItem(22, createButtonItem("§6§lCONFIRM BUY ($${item.buyPrice * amount})", Material.GOLD_INGOT))
    }

    private fun createButtonItem(name: String, material: Material): ItemStack {
        val stack = ItemStack(material)
        val meta = stack.itemMeta
        meta.displayName(Component.text(name))
        stack.itemMeta = meta
        return stack
    }

    private fun createGuiItem(item: ShopItem, amount: Int): ItemStack {
        val material = Material.matchMaterial(item.name) ?: Material.BARRIER
        val stack = ItemStack(material)
        val meta = stack.itemMeta

        meta.displayName(Component.text(item.name))
        meta.lore(listOf(
            Component.text("§aLeft-Click to Buy: $${item.buyPrice * amount}"),
            Component.text("§cRight-Click to Sell: $${item.sellPrice * amount}")
        ))
        stack.itemMeta = meta
        return stack
    }

    override fun getInventory(): Inventory = inv
}