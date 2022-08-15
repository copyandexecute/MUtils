@file:Suppress("unused")

package de.miraculixx.mutils.modules.gui

import de.miraculixx.mutils.enums.settings.gui.GUI
import de.miraculixx.mutils.modules.creator.gui.CreatorDelete
import de.miraculixx.mutils.modules.creator.gui.CreatorEditor
import de.miraculixx.mutils.modules.creator.gui.CreatorList
import de.miraculixx.mutils.modules.creator.gui.CreatorMain
import de.miraculixx.mutils.utils.msg
import de.miraculixx.mutils.utils.premium
import de.miraculixx.mutils.utils.tools.error
import net.axay.kspigot.event.listen
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object GUIListener {

    private val handelClickEvent = listen<InventoryClickEvent> {
        if (it.whoClicked.type != EntityType.PLAYER) return@listen
        val player = it.whoClicked as Player
        val title = it.view.title
        if (!idChecker(it.currentItem)) return@listen
        when {
            title == GUI.SELECT_MENU.title -> {
                it.isCancelled = true
                SelectGUI(it, player)
            }

            title == GUI.CHALLENGE.title -> {
                it.isCancelled = true
                ChallengeGUI(it, player)
            }

            title.startsWith("§9Challenge ∙ ") -> {
                it.isCancelled = true
                SettingsGUI(it, player, title)
            }

            title == GUI.TIMER_SETTINGS.title -> {
                it.isCancelled = true
                TimerGUI(it, player)
            }

            title == GUI.TIMER_DESIGN.title -> {
                it.isCancelled = true
                TimerDesignGUI(it, player)
            }

            title.startsWith("§9Timer ∙ ") -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                TimerOptionsGUI(it, player, title)
            }

            title == GUI.SPEEDRUN_SETTINGS.title -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                SpeedrunGUI(it, player)
            }

            title.startsWith("§9World ") -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                WorldGUI(it, player, title)
            }

            title.startsWith("§9Server Settings") -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                ServerSettingsGUI(it, player, title)
            }

            title == GUI.CREATOR_MAIN.title -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                CreatorMain(it)
            }

            title == GUI.CREATOR_LIST.title -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                CreatorList(it)
            }

            title == GUI.CREATOR_DELETE.title -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                CreatorDelete(it)
            }

            title == GUI.CREATOR_MODIFY.title -> {
                it.isCancelled = true
                if (!verify(player)) return@listen
                CreatorEditor(it)
            }

            else -> return@listen
        }
    }

    private fun idChecker(i: ItemStack?): Boolean {
        if (i == null) return false
        if (!i.hasItemMeta()) return false
        return i.itemMeta?.hasCustomModelData() == true
    }

    fun verify(player: Player): Boolean {
        return if (!premium) {
            player.closeInventory()
            player.error()
            player.sendMessage(msg("command.verify.noPremium"))
            false
        } else true
    }
}
