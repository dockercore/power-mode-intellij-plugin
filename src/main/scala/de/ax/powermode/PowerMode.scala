/*
 * Copyright 2015 Baptiste Mesta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ax.powermode

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.{
  ApplicationComponent,
  PersistentStateComponent,
  State,
  Storage
}
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.util.xmlb.XmlSerializerUtil
import de.ax.powermode.PowerMode.logger
import de.ax.powermode.power.color.ColorEdges
import de.ax.powermode.power.management.ElementOfPowerContainerManager
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.apache.log4j._
import org.jetbrains.annotations.Nullable
import squants.Dimensionless
import squants.DimensionlessConversions.{DimensionlessConversions, each}
import squants.time.Time._
import squants.time.Frequency._
import squants.time.TimeConversions._
import squants.time.FrequencyConversions._
import squants.MetricSystem._
import squants.time.{Frequency, Time, TimeUnit}

import scala.language.postfixOps
import java.awt.event.InputEvent
import java.io.File
import javax.swing.KeyStroke
import scala.collection.immutable.Seq
import scala.util.Try

/**
  * @author Baptiste Mesta
  */
object PowerMode {

  val logger: Logger = Logger.getLogger(classOf[PowerMode])

  @Nullable def getInstance: PowerMode = {
    try {
      ApplicationManager.getApplication.getComponent(classOf[PowerMode])
    } catch {
      case e: Throwable =>
        logger.debug("error getting component: " + e.getMessage(), e)
        null
    }
  }

  def obtainColorEdges(pm: PowerMode): ColorEdges = {
    import pm._
    val edges = new ColorEdges()
    edges.setAlpha(getColorAlpha)
    edges.setRedFrom(getRedFrom)
    edges.setRedTo(getRedTo)
    edges.setGreenFrom(getGreenFrom)
    edges.setGreenTo(getGreenTo)
    edges.setBlueFrom(getBlueFrom)
    edges.setBlueTo(getBlueTo)
    edges
  }
}

@State(name = "PowerModeII",
       storages = Array(new Storage(file = "$APP_CONFIG$/power.mode.ii.xml")))
class PowerMode
    extends ApplicationComponent
    with PersistentStateComponent[PowerMode] {
  type Timestamp = Time
  type HeatupKey = (Option[KeyStroke], Timestamp)
  val mediaPlayerExists: Try[Class[_]] = Try {
    Class.forName("javax.sound.sampled.SourceDataLine")
  }
  var isSingleBamImagePerEvent: Boolean = false
  var hotkeyHeatup: Boolean = true
  var bamLife: Time = 1000 milliseconds
  var soundsFolder = Option.empty[File]
  var minVolume: Dimensionless = 10.percent
  var maxVolume: Dimensionless = 100.percent

  def getMinVolume: Int = minVolume.toPercent.toInt

  def setMinVolume(value: Int): Unit = {
    minVolume = value.percent
    logger.info(s"Setting min volume ${minVolume}")
    maxVolume = Seq(minVolume, maxVolume).max
  }

  def getMaxVolume: Int = maxVolume.toPercent.toInt

  def setMaxVolume(value: Int): Unit = {
    maxVolume = value.percent
    logger.info(s"Setting max volume ${maxVolume}")
    minVolume = Seq(minVolume, maxVolume).min
  }

  var gravityFactor: Double = 21.21
  var sparkVelocityFactor: Double = 4.36
  var sparkSize = 3
  var sparksEnabled = true
  var frameRate: Frequency = 30 hertz
  var maxFlameSize: Int = 100
  var maxFlameLife: Time = 2000 milliseconds
  var heatupTimeMillis: Time = 10000 milliseconds
  var lastKeys = List.empty[HeatupKey]
  var keyStrokesPerMinute: Frequency = 300 / 1.minutes
  var heatupFactor = 1.0
  var sparkLife = 3000
  var sparkCount = 10
  var shakeRange: Int = 4
  var flamesEnabled: Boolean = true
  var maybeElementOfPowerContainerManager
    : Option[ElementOfPowerContainerManager] =
    Option.empty
  var isBamEnabled: Boolean = true
  var isSoundsPlaying: Boolean = false
  var powerIndicatorEnabled: Boolean = true
  var caretAction: Boolean = false
  var hotkeyWeight: Dimensionless = keyStrokesPerMinute * (3 seconds)
  var redFrom: Int = 0
  var redTo: Int = 255
  var greenFrom: Int = 0
  var greenTo: Int = 255
  var blueFrom: Int = 0
  var blueTo: Int = 255
  var colorAlpha: Int = 164
  var heatupThreshold: Double = 0.0
  var _isCustomFlameImages: Boolean = false
  var _isCustomBamImages: Boolean = false
  var customFlameImageFolder = Option.empty[File]
  var customBamImageFolder = Option.empty[File]
  private var enabled: Boolean = true
  private var shakeEnabled: Boolean = false

  def isHotkeyHeatup: Boolean = hotkeyHeatup

  def setHotkeyHeatup(h: Boolean): Unit = {
    hotkeyHeatup = h
  }

  def flameImageFolder: Option[File] = {
    if (!_isCustomFlameImages) Some(new File("fire/animated/256"))
    else customFlameImageFolder
  }

  def bamImageFolder: Option[File] = {
    if (!_isCustomBamImages) Some(new File("bam")) else customBamImageFolder
  }

  def getFrameRate(): Int = frameRate.toHertz.toInt

  def setFrameRateHertz(f: Int): Unit = {
    frameRate = f hertz
  }

  def increaseHeatup(
      dataContext: Option[DataContext] = Option.empty[DataContext],
      keyStroke: Option[KeyStroke] = Option.empty[KeyStroke]): Unit = {
    val ct = System.currentTimeMillis().milliseconds
    lastKeys = (keyStroke, ct) :: filterLastKeys(ct)
    dataContext.foreach(dc =>
      maybeElementOfPowerContainerManager.foreach(_.showIndicator(dc)))

  }

  var previousValues = List.empty[Double]

  def reduceHeatup: Unit = {
    val ct = System.currentTimeMillis().milliseconds
    lastKeys = filterLastKeys(ct)
    adjustValueFactor
  }

  private def adjustValueFactor: Unit = {
    if (previousValues.size > frameRate.toHertz) {
      previousValues = previousValues.dropRight(1)
    }
    val unlimited = rawValueFactorUnlimited
    val wouldValues = unlimited :: previousValues
    val slope = if (wouldValues.size > 1) {
      val s = new SimpleRegression()
      wouldValues.zipWithIndex.foreach {
        case (e, i) =>
          s.addData(i, e)
      }
      s.getSlope
    } else {
      0
    }
    val maxSlope = 0.001
    val maxSlopeValue = 0.0005
    if (slope > maxSlope) {
      rvf *= (1.0 + maxSlopeValue)
    } else if (slope < -maxSlope) {
      rvf *= (1.0 - maxSlopeValue)
    } else {
      rvf = unlimited
    }
    previousValues ::= rvf
  }

  private def filterLastKeys(currentTime: Time): List[HeatupKey] = {
    lastKeys.filter(_._2 >= currentTime - heatupTimeMillis)
  }
  var rvf: Double = 0.0
  def rawValueFactor: Double = { rvf }
  def rawValueFactorUnlimited: Double = {
    val base = heatupFactor +
      ((1 - heatupFactor) * rawTimeFactorFromKeyStrokes)
    (base - heatupThreshold) / (1 - heatupThreshold)
  }

  def rawTimeFactorFromKeyStrokes: Double = {
    val tf = Try {
      if (heatupTimeMillis < 1.seconds) {
        1 ea
      } else {
        val MaxKeystrokesOverHeatupTime
          : Dimensionless = heatupTimeMillis * keyStrokesPerMinute
        val vals: Seq[Dimensionless] = lastKeys.map {
          case (Some(ks), _) =>
            val size = Seq(InputEvent.CTRL_DOWN_MASK,
                           InputEvent.ALT_DOWN_MASK,
                           InputEvent.SHIFT_DOWN_MASK)
              .count(m => (ks.getModifiers & m) > 0)
            (size * hotkeyWeight)
          case _ => 1.ea
        }

        val keystrokesOverHeatupTime: Dimensionless = vals.foldLeft(0.ea)(_ + _)
        val res = (keystrokesOverHeatupTime / MaxKeystrokesOverHeatupTime).ea
        res
      }
    }.getOrElse(0 ea)
    tf.toEach
  }

  def valueFactor: Dimensionless = {
    math.min(math.max(rawValueFactor, 0), 1) * 100.percent
  }

  def timeFactor: Dimensionless = {
    math.min(math.max(rawTimeFactorFromKeyStrokes, 0), 1) * 100.percent
  }

  override def initComponent: Unit = {
    PowerMode.logger.debug("initComponent...")
    val editorFactory = EditorFactory.getInstance
    maybeElementOfPowerContainerManager = Some(
      new ElementOfPowerContainerManager)
    maybeElementOfPowerContainerManager.foreach(
      editorFactory.addEditorFactoryListener(_, new Disposable() {
        def dispose: Unit = {}
      }))
    val editorActionManager = EditorActionManager.getInstance
    EditorFactory
      .getInstance()
      .getEventMulticaster
      .addCaretListener(new MyCaretListener())
    maybeElementOfPowerContainerManager.map(
      cm =>
        editorActionManager.getTypedAction.setupRawHandler(
          new MyTypedActionHandler(
            editorActionManager.getTypedAction.getRawHandler)))
    PowerMode.logger.debug("initComponent done")
  }

  override def disposeComponent: Unit = {
    maybeElementOfPowerContainerManager.foreach(_.dispose)
  }

  override def getComponentName: String = {
    "PowerModeII"
  }

  def getState: PowerMode = {
    this
  }

  def loadState(state: PowerMode): Unit = {
    XmlSerializerUtil.copyBean(state, this)
  }

  def isEnabled: Boolean = {
    enabled
  }

  def setEnabled(enabled: Boolean): Unit = {
    this.enabled = enabled
  }

  def isShakeEnabled: Boolean = {
    shakeEnabled
  }

  def setShakeEnabled(shakeEnabled: Boolean): Unit = {
    this.shakeEnabled = shakeEnabled
  }

  def getSparkCount: Int = sparkCount

  def setSparkCount(sparkCount: Int): Unit = {
    this.sparkCount = sparkCount
  }

  def getSparkLife: Int = sparkLife

  def setSparkLife(sparkRange: Int): Unit = {
    this.sparkLife = sparkRange
  }

  def getShakeRange: Int = shakeRange

  def setShakeRange(shakeRange: Int): Unit = {
    this.shakeRange = shakeRange
  }

  def getHeatup: Int = (heatupFactor * 100).toInt

  def setHeatup(heatup: Int): Unit = {
    this.heatupFactor = heatup / 100.0
  }

  def getHeatupTime: Int = heatupTimeMillis.toMilliseconds.toInt

  def setHeatupTime(heatupTime: Int): Unit = {
    this.heatupTimeMillis = math.max(0, heatupTime) milliseconds
  }

  def getFlameLife: Int = {
    maxFlameLife.toMilliseconds.toInt
  }

  def setFlameLife(flameLife: Int): Unit = {
    maxFlameLife = flameLife.milliseconds
  }

  def getmaxFlameSize: Int = {
    maxFlameSize
  }

  def setmaxFlameSize(maxFlameSize: Int): Unit = {
    this.maxFlameSize = maxFlameSize
  }

  def getKeyStrokesPerMinute: Int = {
    (keyStrokesPerMinute * 1.minutes).toEach.toInt
  }

  def setKeyStrokesPerMinute(keyStrokesPerMinute: Int): Unit = {
    this.keyStrokesPerMinute = keyStrokesPerMinute / 1.minutes
  }

  def isFlamesEnabled: Boolean = {
    flamesEnabled
  }

  def setFlamesEnabled(flamesEnabled: Boolean): Unit = {
    this.flamesEnabled = flamesEnabled
  }

  def isSparksEnabled: Boolean = {
    sparksEnabled
  }

  def setSparksEnabled(sparksEnabled: Boolean): Unit = {
    this.sparksEnabled = sparksEnabled
  }

  def getSparkSize: Int = {
    sparkSize
  }

  def setSparkSize(sparkSize: Int): Unit = {
    this.sparkSize = sparkSize
  }

  def getGravityFactor(): Double = gravityFactor

  def setGravityFactor(f: Double): Unit = {
    gravityFactor = f
  }

  def getSparkVelocityFactor(): Double = sparkVelocityFactor

  def setSparkVelocityFactor(f: Double): Unit = {
    sparkVelocityFactor = f
  }

  def getRedFrom: Int = {
    redFrom
  }

  def setRedFrom(redFrom: Int): Unit = {
    if (redFrom <= redTo)
      this.redFrom = redFrom
  }

  def getRedTo: Int = {
    redTo
  }

  def setRedTo(redTo: Int): Unit = {
    if (redTo >= redFrom)
      this.redTo = redTo
  }

  def getGreenTo: Int = {
    greenTo
  }

  def setGreenTo(greenTo: Int): Unit = {
    if (greenTo >= greenFrom)
      this.greenTo = greenTo
  }

  def getGreenFrom: Int = {
    greenFrom
  }

  def setGreenFrom(gf: Int): Unit = {
    if (gf <= greenTo)
      greenFrom = gf
  }

  def getBlueTo: Int = {
    blueTo
  }

  def setBlueTo(blueTo: Int): Unit = {
    if (blueTo >= getBlueFrom)
      this.blueTo = blueTo
  }

  def getBlueFrom: Int = {
    blueFrom
  }

  def setBlueFrom(bf: Int): Unit = {
    if (bf <= blueTo)
      blueFrom = bf
  }

  def getColorAlpha: Int = {
    colorAlpha
  }

  def setColorAlpha(alpha: Int): Unit = {
    colorAlpha = alpha
  }

  def getSoundsFolder: String =
    soundsFolder.map(_.getAbsolutePath).getOrElse("")

  def setSoundsFolder(file: String): Unit = {
    soundsFolder = Option(new File(file))
  }

  def getIsCaretAction: Boolean = {
    caretAction
  }

  def setIsCaretAction(isCaretAction: Boolean): Unit = {
    this.caretAction = isCaretAction
  }

  def getIsSoundsPlaying: Boolean = isSoundsPlaying

  def setIsSoundsPlaying(isSoundsPlaying: Boolean): Unit = {
    this.isSoundsPlaying = isSoundsPlaying
  }

  def getBamLife: Double = bamLife.toMilliseconds

  def setBamLife(l: Long): Unit = {
    bamLife = l.milliseconds
  }

  def getIsBamEnabled: Boolean = isBamEnabled

  def setIsBamEnabled(b: Boolean): Unit = {
    isBamEnabled = b
  }

  def getHeatupThreshold: Int = {
    (heatupThreshold * 100.0).toInt
  }

  def setHeatupThreshold(t: Int): Unit = {
    heatupThreshold = t / 100.0
  }

  def getIsPowerIndicatorEnabled: Boolean = {
    powerIndicatorEnabled
  }

  def setIsPowerIndicatorEnabled(enabled: Boolean): Unit = {
    powerIndicatorEnabled = enabled
  }

  def isCustomFlameImages: Boolean = _isCustomFlameImages

  def setCustomFlameImages(s: Boolean): Unit = {
    _isCustomFlameImages = s
  }

  def isCustomBamImages: Boolean = _isCustomBamImages

  def setCustomBamImages(s: Boolean): Unit = {
    _isCustomBamImages = s
  }

  def getCustomFlameImageFolder: String =
    customFlameImageFolder.map(_.getAbsolutePath).getOrElse("")

  def setCustomFlameImageFolder(file: String): Unit = {
    customFlameImageFolder = Option(new File(file))
  }

  def getCustomBamImageFolder: String =
    customBamImageFolder.map(_.getAbsolutePath).getOrElse("")

  def setCustomBamImageFolder(file: String): Unit = {
    customBamImageFolder = Option(new File(file))
  }

  def getIsSingleBamImagePerEvent(): Boolean = {
    isSingleBamImagePerEvent
  }

  def setIsSingleBamImagePerEvent(isSingleBamImagePerEvent: Boolean): Unit = {
    this.isSingleBamImagePerEvent = isSingleBamImagePerEvent
  }
}
