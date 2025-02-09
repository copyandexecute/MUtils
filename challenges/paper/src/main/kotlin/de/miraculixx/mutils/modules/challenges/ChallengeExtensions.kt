package de.miraculixx.mutils.modules.challenges

import de.miraculixx.api.modules.challenges.Challenges
import de.miraculixx.api.settings.ChallengeSetting
import de.miraculixx.kpaper.items.itemStack
import de.miraculixx.mutils.extensions.enumOf
import de.miraculixx.mutils.gui.items.skullTexture
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

fun Challenges.getIcon(): ItemStack {
    return itemStack(enumOf<Material>(icon.material) ?: Material.BARRIER) {
        icon.texture?.let { itemMeta = (itemMeta as? SkullMeta)?.skullTexture(it.value) }
    }
}

fun ChallengeSetting<*>.getMaterial() = enumOf(materialKey) ?: Material.BARRIER