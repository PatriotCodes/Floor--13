package com.floor13.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Input

import com.floor13.game.core.World
import com.floor13.game.actors.MapActor
import com.floor13.game.actors.CreatureActor
import com.floor13.game.core.actions.MoveAction
import com.floor13.game.core.actions.OpenDoorAction
import com.floor13.game.core.Position
import com.floor13.game.core.creatures.Creature
import com.floor13.game.core.map.Door

class GameScreen(val world: World) : ScreenAdapter() {
    val levelStage = Stage(ExtendViewport(
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
    ))
    val hudStage = Stage() // TODO: pick viewport

    val mapScroller = object: InputAdapter() {
        var x = 0
        var y = 0
        
        override fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
            this.x = x
            this.y = y
            return true
        }

        override fun touchDragged(x: Int, y: Int, pointer: Int): Boolean {
            val dx = this.x - x
            val dy = this.y - y
            this.x = x
            this.y = y
            levelStage.camera.translate(dx.toFloat(), -dy.toFloat(), 0f)
            return true
        }
    }

	val keyboardProcessor = object: InputAdapter() {
		fun scheduleMoveOrDoorOpening(position: Position) {
			val tile = world.currentFloor[position]
			val action =
					if (tile is Door && !tile.opened) {
						OpenDoorAction(world, world.mainCharacter, position)
					} else {
						MoveAction(world, world.mainCharacter, position)
					}
			if (action.isValid)
				world.mainCharacter.nextAction = action
		}
		override fun keyUp(keycode: Int) =
			when (keycode) {
				Input.Keys.UP -> {
					scheduleMoveOrDoorOpening(world.mainCharacter.position.translated(0, 1))
					true
				}
				Input.Keys.DOWN -> {
					scheduleMoveOrDoorOpening(world.mainCharacter.position.translated(0, -1))
					true
				}
				Input.Keys.RIGHT -> {
					scheduleMoveOrDoorOpening(world.mainCharacter.position.translated(1, 0))
					true
				}
				Input.Keys.LEFT -> {
					scheduleMoveOrDoorOpening(world.mainCharacter.position.translated(-1, 0))
					true
				}
				else -> false
			}
	}

	private val creatureActors = mutableMapOf<Creature, CreatureActor>()

    init {
        levelStage.addActor(MapActor(world.currentFloor))
        for (creature in world.creatures) {
			val actor = CreatureActor(creature)
			levelStage.addActor(actor)
			creatureActors.put(creature, actor)
        }

        Gdx.input.inputProcessor = InputMultiplexer(
                mapScroller,
				keyboardProcessor
        )

		levelStage.camera.position.x = world.mainCharacter.position.x * 64f
		levelStage.camera.position.y = world.mainCharacter.position.y * 64f
    }
    
    override fun render(delta: Float) {
		while (world.mainCharacter.nextAction != null) {
			for (action in world.tick())
				when (action) {
					is MoveAction -> {
						creatureActors[action.creature]?.updateBounds()
							?: Gdx.app.error(TAG, "Invalid creature in move action")
					}
				}
			cameraFollowPlayer()
		}
        levelStage.act(delta)
        levelStage.draw()
    }

	companion object {
		val TAG = GameScreen::class.java.simpleName
	}

	fun cameraFollowPlayer() {  // TODO: check
		val cameraX = levelStage.camera.position.x
		val cameraY = levelStage.camera.position.y
		val playerX = world.mainCharacter.position.x * 64f
		val playerY = world.mainCharacter.position.y * 64f
		val maxDistance = 64f
		if (cameraX > playerX) {
			if (cameraX - playerX > maxDistance)
				levelStage.camera.translate(-64f, 0f, 0f)
		} else {
			if (playerX - cameraX > maxDistance)
				levelStage.camera.translate(64f, 0f, 0f)
		}
		if (cameraY > playerY) {
			if (cameraY - playerY > maxDistance)
				levelStage.camera.translate(0f, -64f, 0f)
		} else {
			if (playerY - cameraY > maxDistance)
				levelStage.camera.translate(0f, 64f, 0f)
		}
	}

}
