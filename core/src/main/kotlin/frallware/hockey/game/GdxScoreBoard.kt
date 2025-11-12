package frallware.hockey.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import java.time.Duration


class GdxScoreBoard(
    private val viewport: ScreenViewport,
    private val scores: Map<Side, Int>,
    private val timeLeft: () -> Duration,
) {

    private val stage = Stage(viewport)

    private val font = BitmapFont().apply {
        data.setScale(2f)
    }
    private val scoresLabel: Label = Label("00 - 00", LabelStyle(font, Color.WHITE))
    private val timeLabel: Label = Label("2:33", LabelStyle(font, Color.ORANGE))

    init {
        val table = Table().pad(10f)

        table.add(scoresLabel).expandX().left()
        table.add(timeLabel).expandX().right()
        table.setSize(table.prefWidth, table.prefHeight)
        table.setPosition(20f, stage.height - table.prefHeight - 10f)

        // background color
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.FIREBRICK)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        table.background = TextureRegionDrawable(TextureRegion(texture))

        stage.addActor(table)

    }

    fun render() {
        scoresLabel.setText("${scores[Side.Left]} - ${scores[Side.Right]}")
        val time = timeLeft()
        timeLabel.setText(String.format("%d:%02d", time.toMinutesPart(), time.toSecondsPart()))


        stage.act()
        stage.draw()
    }

    fun dispose() {
        stage.dispose()
    }
}
