package com.devtech.shootoutar

import android.graphics.Point
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var arFragment: CustomArFragment
    private lateinit var scene: Scene
    private lateinit var camera: Camera
    private lateinit var bulletRenderable: ModelRenderable
    private var shouldStartTimer = true
    private var balloonsLeft: Int = 20
    private lateinit var point: Point
    private lateinit var soundPool: SoundPool
    private var sound: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay
        point = Point()
        display.getRealSize(point)

        setContentView(R.layout.activity_main)

        loadSoundPool()

        arFragment =
            (supportFragmentManager.findFragmentById(R.id.arFragment) as CustomArFragment?)!!
        scene = arFragment.arSceneView.scene
        camera = scene.camera

        addYourModelToScene()
        buildBulletModel()

        shootButton.setOnClickListener {
            if (shouldStartTimer) {
                startTimer()
                shouldStartTimer = false
            }
            shoot()
        }
    }

    private fun loadSoundPool() {
        val audioAttribtes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttribtes)
            .build()

        sound = soundPool.load(this, R.raw.blop_sound, 1)


    }

    private fun shoot() {
        /*var ray = camera.screenPointToRay(point.x / 2f, point.y / 2f)
        var node = Node()
        node.renderable = bulletRenderable
        scene.addChild(node)

        Thread(Runnable {
            for (i in 0..200) {
                runOnUiThread {
                    val vector3 = ray.getPoint(i * 0.01f)
                    node.worldPosition = vector3
                    val nodeInContact = scene.overlapTest(node)
                    if (nodeInContact != null) {
                        balloonsLeft--
                        balloonsCntTxt.text = "Balloons Left: $balloonsLeft"
                        scene.removeChild(node)
                        soundPool.play(sound, 1f, 1f, 1, 0, 1f)
                    }

                }
                try {
                    Thread.sleep(10)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            runOnUiThread {
                scene.removeChild(node)
            }

        }).start()*/

        val ray = camera.screenPointToRay(point.x / 2f, point.y / 2f)
        val node = Node()
        node.renderable = bulletRenderable
        scene.addChild(node)

        Thread(Runnable {
            for (i in 0..199) {
                runOnUiThread {
                    val vector3 = ray.getPoint(i * 0.1f)
                    node.worldPosition = vector3
                    val nodeInContact = scene.overlapTest(node)
                    if (nodeInContact != null) {
                        balloonsLeft--
                        balloonsCntTxt.text= "Balloons Left: $balloonsLeft"
                        scene.removeChild(nodeInContact)
                        soundPool.play(
                            sound, 1f, 1f, 1, 0
                            , 1f
                        )
                    }
                }
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            runOnUiThread { scene.removeChild(node) }
        }).start()

    }

    private fun startTimer() {
        Thread(Runnable {
            var seconds = 0
            while (balloonsLeft > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                seconds++
                val minutesPassed = seconds / 60
                val secondsPassed = seconds % 60
                runOnUiThread {
                    timerText.text = "$minutesPassed:$secondsPassed"
                }
            }
        }).start()
    }

    private fun buildBulletModel() {
        Texture
            .builder()
            .setSource(this, R.drawable.texture)
            .build()
            .thenAccept {
                MaterialFactory
                    .makeOpaqueWithTexture(this, it)
                    .thenAccept {
                        bulletRenderable = ShapeFactory
                            .makeSphere(0.01f, Vector3(0f, 0f, 0f), it)
                    }
            }
    }

    private fun addYourModelToScene() {
        ModelRenderable
            .builder()
            .setSource(this, Uri.parse("balloon.sfb"))
            .build()
            .thenAccept {

                for (i in 1..20) {
                    var node = Node()
                    node.renderable = it
                    scene.addChild(node)

                    var random = Random()
                    var x = random.nextInt(10)
                    var z = random.nextInt(10)
                    var y = random.nextInt(20)

                    z = -z

                    node.worldPosition = Vector3(
                        x.toFloat(),
                        y / 10f,
                        z.toFloat()
                    )
                }

            }
    }


}


