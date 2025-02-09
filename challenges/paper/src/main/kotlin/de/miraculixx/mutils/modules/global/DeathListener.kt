package de.miraculixx.mutils.modules.global

import de.miraculixx.kpaper.event.listen
import de.miraculixx.mutils.messages.msg
import de.miraculixx.mutils.messages.namespace
import org.bukkit.NamespacedKey
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.persistence.PersistentDataType

object DeathListener {
    private val onDeath = listen<PlayerDeathEvent> {
        val player = it.player
        val key = NamespacedKey(namespace, "death.custom")
        val deathKey = player.persistentDataContainer.get(key, PersistentDataType.STRING) ?: return@listen
        it.deathMessage(msg("event.death.$deathKey", listOf(it.player.name)))
        player.persistentDataContainer.remove(key)
    }
}