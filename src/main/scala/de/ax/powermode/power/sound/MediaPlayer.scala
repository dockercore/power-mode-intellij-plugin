package de.ax.powermode.power.sound

import de.ax.powermode.PowerMode
import javazoom.jl.player.{FactoryRegistry, HackyJavaSoundAudioDevice}
import javazoom.jl.player.advanced.{
  AdvancedPlayer,
  PlaybackEvent,
  PlaybackListener
}
import org.apache.log4j.Logger
import squants.Dimensionless
import squants.DimensionlessConversions.dimensionlessToDouble

import scala.util.{Try, Using}
import java.io.{
  BufferedInputStream,
  File,
  FileInputStream,
  FileOutputStream,
  InputStream
}
import javax.sound.sampled.{Control, FloatControl}

object MediaPlayer {
  def getSoundAudioDevice =
    new HackyJavaSoundAudioDevice

}

class MediaPlayer(file: File, volumeRange: => (Dimensionless, Dimensionless))
    extends AutoCloseable {
  def logger: Logger = PowerMode.logger
  val stream: BufferedInputStream = new BufferedInputStream(
    new FileInputStream(file))
  val soundAudioDevice: HackyJavaSoundAudioDevice =
    MediaPlayer.getSoundAudioDevice
  val player: AdvancedPlayer = new AdvancedPlayer(stream, soundAudioDevice)
  val listener: PlaybackListener = new PlaybackListener {
    override def playbackStarted(evt: PlaybackEvent): Unit = {
      logger.debug("playbackStarted")
    }

    override def playbackFinished(evt: PlaybackEvent): Unit = {
      notifyHandlers()
    }
  }
  var playThread = Option.empty[Thread]
  player.setPlayBackListener(listener)

  def setVolume(rawGain: Dimensionless): Unit = {
    logger.trace("setting volume ")
    val control: Option[FloatControl] = Option(soundAudioDevice.source)
      .flatMap(sourceDataLine =>
        Try {
          sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN)
        }.toOption)
      .map(_.asInstanceOf[FloatControl])
    control.foreach { volControl =>
      val range: (Dimensionless, Dimensionless) = volumeRange
      val volRange = volControl
        .getMaximum() - volControl.getMinimum()

      val gain: Dimensionless =
        if (range._2 < rawGain) {
          range._2
        } else if (rawGain < range._1) {
          range._1
        } else {
          rawGain
        }

      // use log scale to have a smoother transition at higer volume level
      // slight higher volume changes are much harsher compared to the same amount of low volume change otherwise.
      val logGain: Double = math.log10(1 + gain.toDouble * 9)
      val newGain: Double =
        Math.min(Math.max(volControl.getMinimum() + (logGain * volRange),
                          volControl.getMinimum()),
                 volControl.getMaximum() * 0.99999999)

      logger.trace(
        s"setting volume ${rawGain}factor. was limited to ${gain} applied to ${volControl
          .getMinimum()} - ${volControl.getMaximum()}  => ${newGain}")
      logger.trace("Was: " + volControl.getValue() + " Will be: " + newGain);
      volControl.setValue(newGain.toFloat);
    }
  }

  def play(): Unit = {
    if (playThread.isEmpty) {
      logger.debug("starting")
      playThread = Option(new Thread(new Runnable() {
        override def run(): Unit = {
          try {
            player.play()
          } catch {
            case e =>
              logger.error("playback error", e)
              notifyHandlers()
              throw e
          } finally {
            playThread = None
          }
        }
      }))
      playThread.foreach(_.start())
      logger.debug("started")

    }
  }

  private def notifyHandlers(): Unit = {
    handlers.foreach(_.apply())
  }

  def stop(): Unit = {
    if (player != null) {
      player.stop()
    }
  }
  var handlers = List.empty[() => Unit]
  def onError(fn: () => Unit): Unit = {
    handlers ::= fn
  }
  def close(): Unit = {
    try {
      stop()
    } finally {
      player.close()
      stream.close()
    }
  }
}
