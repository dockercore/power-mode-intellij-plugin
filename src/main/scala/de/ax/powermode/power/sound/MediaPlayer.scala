package de.ax.powermode.power.sound

import de.ax.powermode.PowerMode
import javazoom.jl.player.{FactoryRegistry, HackyJavaSoundAudioDevice}
import javazoom.jl.player.advanced.{
  AdvancedPlayer,
  PlaybackEvent,
  PlaybackListener
}

import scala.util.Using
import java.io.{
  BufferedInputStream,
  File,
  FileInputStream,
  FileOutputStream,
  InputStream
}
import javax.sound.sampled.{Control, FloatControl}

object MediaPlayer {
  lazy val soundAudioDevice =
    new HackyJavaSoundAudioDevice

}

class MediaPlayer(file: File) extends AutoCloseable {
  def logger = PowerMode.logger
  import de.ax.powermode.power.sound.MediaPlayer._
  val stream = new BufferedInputStream(new FileInputStream(file))
  val player = new AdvancedPlayer(stream, soundAudioDevice)
  val listener = new PlaybackListener {
    override def playbackStarted(evt: PlaybackEvent): Unit = {
      logger.debug("playbackStarted")
    }

    override def playbackFinished(evt: PlaybackEvent): Unit = {
      notifyHandlers()
    }
  }
  var playThread = Option.empty[Thread]
  player.setPlayBackListener(listener)

  def setVolume(gain: Float) = {
    logger.trace("setting volume ")
    val control: Option[FloatControl] = Option(soundAudioDevice.source)
      .flatMap(x => Option(x.getControl(FloatControl.Type.MASTER_GAIN)))
      .map(_.asInstanceOf[FloatControl])
    control.foreach { volControl =>
      val volRange = volControl
        .getMaximum() - volControl.getMinimum()
      val newGain: Double =
        Math.min(Math.max(volControl.getMinimum() + ((gain) * volRange),
                          volControl.getMinimum()),
                 volControl.getMaximum() * 0.99999999)

      logger.debug(s"setting volume ${gain}factor applied to ${volControl
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
              notifyHandlers()
              throw e
          }
        }
      }))
      playThread.foreach(_.start())
      logger.debug("started")

    }
  }

  private def notifyHandlers() = {
    handlers.foreach(_.apply())
  }

  def stop(): Unit = {
    player.stop()
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
