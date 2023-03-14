package de.miraculixx.mutils.modules.mods.noSameItems

import de.miraculixx.api.modules.challenges.Challenge
import de.miraculixx.api.modules.challenges.Challenges
import de.miraculixx.api.modules.mods.noSameItem.NoSameItemEnum
import de.miraculixx.api.settings.challenges
import de.miraculixx.api.settings.getSetting
import de.miraculixx.kpaper.event.listen
import de.miraculixx.kpaper.event.register
import de.miraculixx.kpaper.event.unregister
import de.miraculixx.kpaper.extensions.onlinePlayers
import de.miraculixx.mutils.extensions.enumOf
import de.miraculixx.mutils.messages.*
import de.miraculixx.mutils.modules.spectator.Spectator
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import java.time.Duration
import java.util.UUID

class NoSameItem : Challenge {
    override val challenge = Challenges.NO_SAME_ITEM
    private val infoMode: NoSameItemEnum
    private val lives: Int
    private val sync: Boolean

    private val playerCollections: MutableMap<UUID, MutableSet<Material>> = mutableMapOf()
    private val collectionOrder: MutableMap<Material, MutableList<UUID>> = mutableMapOf()
    private val barMap: MutableMap<UUID, BossBar> = mutableMapOf()
    private val hearts: MutableMap<UUID, Int> = mutableMapOf()

    init {
        val settings = challenges.getSetting(Challenges.NO_SAME_ITEM).settings
        infoMode = enumOf<NoSameItemEnum>(settings["info"]?.toEnum()?.getValue() ?: "EVERYTHING") ?: NoSameItemEnum.EVERYTHING
        lives = settings["lives"]?.toInt()?.getValue() ?: 5
        sync = settings["sync"]?.toBool()?.getValue() ?: false
    }

    override fun start(): Boolean {
        onlinePlayers.forEach { player ->
            player.showBossBar(getBossBar(player.uniqueId))
            if (sync) player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = lives * 2.0
        }
        return true
    }

    override fun stop() {
        barMap.forEach { (uuid, bar) ->
            Bukkit.getPlayer(uuid)?.hideBossBar(bar)
        }
        barMap.clear()
        collectionOrder.clear()
        playerCollections.clear()
    }

    override fun register() {
        onJoin.register()
        onDie.register()
        onCraft.register()
        onInvClose.register()
        onCollect.register()
    }

    override fun unregister() {
        onJoin.unregister()
        onDie.unregister()
        onCraft.unregister()
        onInvClose.unregister()
        onCollect.unregister()
    }

    private val onJoin = listen<PlayerJoinEvent>(register = false) {
        val uuid = it.player.uniqueId
        if (Spectator.isSpectator(uuid)) return@listen
        it.player.showBossBar(getBossBar(uuid))
    }

    private val onDie = listen<PlayerDeathEvent>(register = false) {
        manager.removePlayer(it.entity)
    }

    private val onCraft = listen<CraftItemEvent>(register = false) {
        val player = it.whoClicked as? Player ?: return@listen
        if (Spectator.isSpectator(player.uniqueId)) return@listen
        val item = it.currentItem ?: return@listen
        addItems(player, setOf(item.type), "Crafting")
    }

    private val onInvClose = listen<InventoryCloseEvent>(register = false) {
        if (Spectator.isSpectator(it.player.uniqueId)) return@listen
        val view = it.view
        if (view.bottomInventory.type != InventoryType.PLAYER) return@listen
        val player = it.player as? Player ?: return@listen
        val before = mutableSetOf<Material>()
        val after = mutableSetOf<Material>()
        // Add all registered items to 'before'
        before.addAll(playerCollections.getOrElse(player.uniqueId) { emptySet() })

        // Add all inventory items to 'after'
        after.addAll(player.inventory.mapNotNull { item -> item.type })
        val topInv = view.topInventory
        if (topInv.type == InventoryType.CRAFTING || it.view.topInventory.type == InventoryType.WORKBENCH) {
            topInv.setItem(0, null)
            after.addAll(topInv.mapNotNull { item -> item.type })
        }
        val cursor = view.cursor
        if (cursor != null && !cursor.type.isAir) after.add(cursor.type)

        // Remove all items from before and only keep new items
        after.removeAll(before)
        addItems(player, after, "Inventory")
    }

    private val onCollect = listen<EntityPickupItemEvent>(register = false) {
        val player = it.entity as? Player ?: return@listen
        if (Spectator.isSpectator(player.uniqueId)) return@listen
        addItems(player, setOf(it.item.itemStack.type), "Collect")
    }


    private fun getBossBar(uuid: UUID): BossBar {
        return barMap.getOrPut(uuid) {
            BossBar.bossBar(msg("event.noSameItem.deathTitle"), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
        }
    }

    private fun addItems(player: Player, items: Set<Material>, type: String) {
        var dupes = 0
        val uuid = player.uniqueId
        items.forEach { item ->
            val collected = playerCollections.getOrPut(uuid) { mutableSetOf() }
            if (collected.contains(item)) return //Player already collected this item
            collected.add(item)
            val order = collectionOrder.getOrPut(item) { mutableListOf() }
            if (order.isNotEmpty()) { //Another player already collected it
                dupes++
                val firstPlayer = Bukkit.getPlayer(order.first())
                player.sendMessage(prefix + msg("event.noSameItem.duplicate", listOf(firstPlayer?.name ?: "Unknown", item.name)))
            }
            order.add(uuid)
            if (infoMode == NoSameItemEnum.EVERYTHING) player.sendMessage(cmp("+ ", cSuccess) + cmp(item.name) + cmp("($type)", NamedTextColor.DARK_GRAY))
        }

        if (dupes > 0) {
            val title = cmp("- ", cError) + cmp(buildString { repeat(dupes) { append("❤") } }, NamedTextColor.DARK_RED)
            val duration = Duration.ofMillis(500)
            player.showTitle(Title.title(emptyComponent(), title, Title.Times.times(duration, duration, duration)))
            val newHearts = hearts.getOrPut(uuid) { 1 } - dupes
            hearts[uuid] = newHearts

            if (newHearts <= 0) {
                player.persistentDataContainer.set(NamespacedKey(namespace, "death.custom"), PersistentDataType.STRING, "noSameItem")
                player.damage(999.0)
            } else player.damage(0.01)
        }
    }

    private fun removePlayer(player: Player) {
        val uuid = player.uniqueId
        val items = playerCollections[uuid] ?: return
        //Check if player is first on any item and remove him from the order
        val heartsBack = buildMap {
            items.forEach { item ->
                val order = collectionOrder[item] ?: return@forEach
                if (order.firstOrNull() == uuid) { //Player is first
                    order.getOrNull(1)?.let { set(uuid,  getOrElse(uuid) { 0 } + 1) }
                }
                order.remove(uuid)
            }
        }
        items.clear()
    }
}