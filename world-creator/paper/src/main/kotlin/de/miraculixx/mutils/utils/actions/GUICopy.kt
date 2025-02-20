package de.miraculixx.mutils.utils.actions

import de.miraculixx.kpaper.items.customModel
import de.miraculixx.mutils.await.AwaitChatMessage
import de.miraculixx.mutils.extensions.click
import de.miraculixx.mutils.extensions.soundEnable
import de.miraculixx.mutils.gui.GUIEvent
import de.miraculixx.mutils.gui.data.CustomInventory
import de.miraculixx.mutils.messages.emptyComponent
import de.miraculixx.mutils.messages.msg
import de.miraculixx.mutils.messages.title
import de.miraculixx.mutils.module.WorldManager
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class GUICopy(world: World, gui: CustomInventory): GUIEvent {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, _: CustomInventory ->
        it.isCancelled = true
        val player = it.whoClicked as? Player ?: return@event
        val item = it.currentItem ?: return@event

        when (val id = item.itemMeta?.customModel) {
            1, 2 -> {
                AwaitChatMessage(true, player, "world name", 60, null, {
                    player.closeInventory()
                    player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 99999, 1, false, false, false))
                    player.playSound(player, Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 0.8f)
                    player.title(msg("event.newWorld", listOf(it)), msg("event.newWorldSub", listOf(it)), 0.5.seconds, 1.hours)
                    if (id == 1) WorldManager.copyWorld(world.uid, it) else WorldManager.fullCopyWorld(world.uid, it)
                    player.soundEnable()
                    player.title(emptyComponent(), emptyComponent())
                    player.removePotionEffect(PotionEffectType.BLINDNESS)
                    gui.update()
                }) {
                    gui.open(player)
                }
            }

            else -> {
                player.click()
                gui.open(player)
            }
        }
    }
}