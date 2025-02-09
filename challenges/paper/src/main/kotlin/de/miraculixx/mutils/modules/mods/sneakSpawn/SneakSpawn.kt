package de.miraculixx.mutils.modules.mods.sneakSpawn

import de.miraculixx.api.modules.challenges.Challenge
import de.miraculixx.api.modules.challenges.Challenges
import de.miraculixx.api.settings.challenges
import de.miraculixx.api.settings.getSetting
import de.miraculixx.kpaper.event.listen
import de.miraculixx.kpaper.event.register
import de.miraculixx.kpaper.event.unregister
import de.miraculixx.mutils.utils.getLivingMobs
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack

class SneakSpawn : Challenge {
    override val challenge = Challenges.SNEAK_SPAWN
    private val mobs: Boolean
    private var livings: MutableList<EntityType> = mutableListOf()
    private var materials: MutableList<Material> = mutableListOf()

    init {
        val settings = challenges.getSetting(challenge).settings
        mobs = settings["onlyMob"]?.toBool()?.getValue() ?: true
    }

    override fun start(): Boolean {
        //Create List of Living Entitys
        if (mobs) {
            val list = getLivingMobs(false)
            list.remove(EntityType.PLAYER)
            livings = list
        } else {
            val list = ArrayList<Material>()
            Material.values().forEach { material ->
                if (material.isItem) {
                    list.add(material)
                }
            }
            materials = list
        }

        return true
    }

    override fun stop() {
        materials.clear()
        livings.clear()
    }

    override fun register() {
        onSneak.register()
    }

    override fun unregister() {
        onSneak.unregister()
    }

    private val onSneak = listen<PlayerToggleSneakEvent>(register = false) {
        if (it.isSneaking) return@listen
        val player = it.player
        val pos = player.location
        if (mobs) {
            val type = livings.random()
            pos.world.spawnEntity(pos, type)
        } else {
            val type = materials.random()
            val item = pos.world.dropItem(pos, ItemStack(type))
            item.pickupDelay = 30
            item.isGlowing = true
        }
    }
}