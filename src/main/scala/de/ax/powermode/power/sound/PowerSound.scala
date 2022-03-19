package de.ax.powermode.power.sound

import de.ax.powermode.Power
import javazoom.jl.player.{FactoryRegistry, HackyJavaSoundAudioDevice, Player}
import squants.Dimensionless

import java.io.File
import scala.language.postfixOps

/**
  * Created by nyxos on 03.10.16.
  */
class PowerSound(folder: => Option[File],
                 valueFactor: => Dimensionless,
                 volumeRange: => (Dimensionless, Dimensionless))
    extends Power {
  def next(): Unit = {
    this.synchronized {
      doStop()
      doPlay()
    }
  }

  def resetPlaying(): Unit = {
    playing = false
  }

  def files: Array[File] =
    folder
      .flatMap(f => Option(f.listFiles()))
      .getOrElse(Array.empty[File])
      .filter(f => f.isFile && f.exists)

  var playing = false

  var current = 1

  def setVolume(v: Dimensionless): Unit = this.synchronized {
    mediaPlayer.foreach(_.setVolume(v))
  }

  private def doStop(): Unit = {
    this.synchronized {
      mediaPlayer.foreach(_.stop())
      while (mediaPlayer.exists(_.playThread.exists(_.isAlive))) {
        logger.info("still playing in thread")
        Thread.sleep(100)
      }
      mediaPlayer = Option.empty
      playing = false
    }
  }

  var mediaPlayer = Option.empty[MediaPlayer]

  var index = 0

  var lastFolder: Option[File] = folder

  def stop(): Unit = this.synchronized {
    doStop()
  }

  def play(): Unit = this.synchronized {
    if (lastFolder.map(_.getAbsolutePath) != folder.map(_.getAbsolutePath)) {
      doStop()
    }
    doPlay()
  }

  private def doPlay(): Unit = {
    val myFiles: Array[File] = files
    if (!playing && myFiles != null && !myFiles.isEmpty) {
      index = (Math.random() * (200 * myFiles.length)).toInt % myFiles.length
      val f = myFiles(index)
      logger.info(
        s"${this.hashCode()} playing sound file '$f' playing=${playing} mediaPlayer=${mediaPlayer}")
      try {
        playing = true
        mediaPlayer = Some {
          val mediaPlayer =
            new de.ax.powermode.power.sound.MediaPlayer(f, volumeRange)
          mediaPlayer.onError(() => {
            logger.debug("resetting")
            resetPlaying()
          })
          mediaPlayer.setVolume(valueFactor)
          mediaPlayer.play()
          mediaPlayer
        }
        logger.info(s"started playing '$f'")
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          playing = false
      }
    }
  }
}
