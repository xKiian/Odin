package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.getBlockIdAt
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object BoulderSolver {
    private data class BoxPosition(val render: BlockPos, val click: BlockPos)
    private var currentPositions = mutableListOf<BoxPosition>()
    private var solutions: Map<String, List<List<Int>>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/boulderSolutions.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            solutions = gson.fromJson(text, object : TypeToken<Map<String, List<List<Int>>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            solutions = emptyMap()
        }
    }

    fun onRoomEnter(event: DungeonEvents.RoomEnterEvent) {
        val room = event.room?.room ?: return reset()
        if (room.data.name != "Boulder") return reset()
        var str = ""
        for (z in -3..2) {
            for (x in -3..3) {
                room.vec2.addRotationCoords(room.rotation, x * 3, z * 3).let {
                    str += if (getBlockIdAt(it.x, 66, it.z) == 0) "0" else "1"
                }
            }
        }
        devMessage(str)
        val coords = solutions[str] ?: return
        currentPositions = coords.map { sol ->
            val render = room.vec2.addRotationCoords(room.rotation, sol[0], sol[1])
            val click = room.vec2.addRotationCoords(room.rotation, sol[2], sol[3])
            BoxPosition(BlockPos(render.x, 65, render.z), BlockPos(click.x, 65, click.z))
        }.toMutableList()
        devMessage(currentPositions)
    }

    fun onRenderWorld() {
        currentPositions.forEach {
            Renderer.drawStyledBlock(it.render, PuzzleSolvers.boulderColor, PuzzleSolvers.boulderStyle, PuzzleSolvers.boulderLineWidth)
        }
    }

    fun playerInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return
        if (!getBlockIdAt(event.pos).equalsOneOf(77, 323)) return
        currentPositions.removeIf { it.click == event.pos }
    }

    fun reset() {
        currentPositions = mutableListOf()
    }

}