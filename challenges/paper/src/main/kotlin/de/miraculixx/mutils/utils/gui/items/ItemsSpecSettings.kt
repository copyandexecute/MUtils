package de.miraculixx.mutils.utils.gui.items

import de.miraculixx.api.modules.spectator.Activation
import de.miraculixx.api.modules.spectator.Visibility
import de.miraculixx.kpaper.items.customModel
import de.miraculixx.kpaper.items.itemStack
import de.miraculixx.kpaper.items.meta
import de.miraculixx.kpaper.items.name
import de.miraculixx.mutils.gui.items.ItemProvider
import de.miraculixx.mutils.messages.*
import de.miraculixx.mutils.modules.spectator.SpecCollection
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemsSpecSettings(private val settings: SpecCollection) : ItemProvider {
    private val hidden = msgString("event.hidden")
    private val shown = msgString("event.shown")
    private val disabled = msgString("event.disabled")
    private val enabled = msgString("event.enabled")
    private val cGrey = NamedTextColor.DARK_GRAY

    private val msgInfo = listOf(emptyComponent(), cmp("∙ ") + cmp("Info", cHighlight, underlined = true))
    private val msgToggle = listOf(emptyComponent(), msgClick + cmp("Toggle"))

    override fun getSlotMap(): Map<ItemStack, Int> {
        return mapOf(
            itemStack(Material.ENDER_EYE) {
                getToggleAble(1, "items.spec.visibility", settings.hide == Visibility.HIDDEN, true)
            } to 10,
            itemStack(if (majorVersion >= 17) Material.SPYGLASS else Material.ENDER_EYE) {
                getToggleAble(2, "items.spec.visibilityOther", settings.selfHide == Visibility.HIDDEN, true)
            } to 11,
            itemStack(Material.FEATHER) {
                meta {
                    customModel = 3
                    name = cmp(msgString("items.spec.flySpeed.n"), cHighlight) + cmp(" (${settings.flySpeed})")
                    lore(
                        msgInfo + msgList("items.spec.flySpeed.l") + listOf(
                            msgClickRight + cmp("-1"),
                            msgClickLeft + cmp("+1")
                        )
                    )
                }
            } to 13,
            itemStack(Material.HOPPER) {
                getToggleAble(4, "items.spec.items", settings.itemPickup == Activation.DISABLED, false)
            } to 15,
            itemStack(Material.DIAMOND_PICKAXE) {
                getToggleAble(5, "items.spec.blocks", settings.blockBreak == Activation.DISABLED, false)
                meta { addItemFlags(ItemFlag.HIDE_ATTRIBUTES) }
            } to 16,
        )
    }

    private fun ItemStack.getToggleAble(id: Int, key: String, red: Boolean, isVisibility: Boolean) {
        meta {
            customModel = id
            val rawName = msgString("$key.n")
            name = if (red) cmp(rawName, cError) + cmp(" (${if (isVisibility) hidden else disabled})", cGrey) else cmp(rawName, cSuccess) + cmp(" (${if (isVisibility) shown else enabled})", cGrey)
            lore(msgInfo + msgList("$key.l") + msgToggle)
        }
    }
}