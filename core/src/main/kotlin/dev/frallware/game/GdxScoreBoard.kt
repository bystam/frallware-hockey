package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import dev.frallware.Constants

class GdxScoreBoard(
    private val viewport: Viewport,
    private val scores: Map<Side, Int>
) {
    val font = BitmapFont().apply {
        color = Color.RED
        data.setScale(0.2f)
    }

    fun render() {
        val batch = SpriteBatch()
        batch.projectionMatrix = viewport.camera.combined

        batch.begin()
        font.draw(
            /* batch = */ batch,
            /* str = */ "${scores[Side.Left]}      ${scores[Side.Right]}",
            /* x = */ Constants.WORLD_WIDTH / 2,
            /* y = */ 3f,
            /* targetWidth = */ 2f,
            /* halign = */ Align.center,
            /* wrap = */ false
        )
        batch.end()
    }
}
