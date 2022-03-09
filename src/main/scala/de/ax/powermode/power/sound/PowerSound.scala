package de.ax.powermode.power.sound

import de.ax.powermode.Power
import javazoom.jl.player.{FactoryRegistry, HackyJavaSoundAudioDevice, Player}

import java.io.File
import scala.language.postfixOps

/**
  * Created by nyxos on 03.10.16.
  */
class PowerSound(folder: => Option[File], valueFactor: => Double)
    extends Power {
  def next(): Unit = {
    this.synchronized {
      doStop()
      doPlay()
    }
  }

  val ResetPlaying: Runnable = new Runnable {
    override def run(): Unit = playing = false
  }

  def files =
    folder
      .flatMap(f => Option(f.listFiles()))
      .getOrElse(Array.empty[File])
      .filter(f => f.isFile && f.exists)

  var playing = false

  var current = 1

  def setVolume(v: Double) = this.synchronized {
    mediaPlayer.foreach(_.setVolume((0.75 * v * v) + (0.25 * v) toFloat))
  }

  private def doStop() = {
    this.synchronized {
      mediaPlayer.foreach(_.stop())
      playing = false
    }
  }

  var mediaPlayer = Option.empty[MediaPlayer]

  var index = 0

  var lastFolder = folder

  def stop() = this.synchronized {
    doStop()
  }

  def play() = this.synchronized {
    doPlay()
  }

  private def doPlay() = {
    if (lastFolder.map(_.getAbsolutePath) != folder.map(_.getAbsolutePath)) {
      mediaPlayer.foreach(_.stop())
      playing = false
    }

    val myFiles: Array[File] = files
    if (!playing && myFiles != null && !myFiles.isEmpty) {
      index = (Math.random() * (200 * myFiles.length)).toInt % myFiles.length
      val f = myFiles(index)
      logger.info(s"playing sound file '$f'")
      try {
        playing = true
        mediaPlayer = Some {
          val mediaPlayer = new de.ax.powermode.power.sound.MediaPlayer(f)
          mediaPlayer.onError(() => {
            logger.debug("resetting")
            ResetPlaying.run()
          })
          mediaPlayer.setVolume(valueFactor.toFloat)
          mediaPlayer.play()
          mediaPlayer
        }
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          playing = false
      }
    }
  }
}
