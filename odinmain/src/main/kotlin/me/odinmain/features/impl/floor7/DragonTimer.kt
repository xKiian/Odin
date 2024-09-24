package me.odinmain.features.impl.floor7

import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer

object DragonTimer {

    fun renderTime() {
        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.state != WitherDragonState.SPAWNING) return@forEachIndexed

            Renderer.drawStringInWorld(
                "§${dragon.colorCode}${dragon.name.first()}: ${colorDragonTimer(dragon.timeToSpawn / 20.0)}", dragon.spawnPos,
                color = Color.WHITE, depth = false,
                scale = 0.16f
            )
        }
    }

    fun colorDragonTimer(spawnTime: Double): String {
        return when {
            spawnTime <= 1.0 -> "§c$spawnTime"
            spawnTime <= 3.0 -> "§e$spawnTime"
            else -> "§a$spawnTime"
        }
    }
}