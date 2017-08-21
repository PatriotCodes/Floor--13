package com.floor13.game.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport

import com.floor13.game.core.World

class GameScreen(world: World) : ScreenAdapter() {
    val levelStage = Stage(ExtendViewport(0f, 0f))
    val hudStage = Stage() // TODO: pick viewport
    
    override fun render(delta: Float) {
        levelStage.act(delta)
        levelStage.draw()
    }
}
