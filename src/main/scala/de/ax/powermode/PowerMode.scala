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
import com.intellij.openapi.components.{ApplicationComponent, PersistentStateComponent, State, Storage}
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.util.xmlb.XmlSerializerUtil
import de.ax.powermode.power.color.ColorEdges
import de.ax.powermode.power.management.ElementOfPowerContainerManager
import org.apache.log4j._
import org.jetbrains.annotations.Nullable

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
  type HeatupKey = (Option[KeyStroke], Long)
  val mediaPlayerExists = Try {
    Class.forName("javax.sound.sampled.SourceDataLine")
  }
  var isSingleBamImagePerEvent: Boolean = false
  var hotkeyHeatup: Boolean = true
  var bamLife: Long = 1000
  var soundsFolder = Option.empty[File]
  var gravityFactor: Double = 21.21
  var sparkVelocityFactor: Double = 4.36
  var sparkSize = 3
  var sparksEnabled = true
  var frameRate: Int = 30
  var maxFlameSize = 100
  var maxFlameLife = 2000
  var heatupTime = 10000
  var lastKeys = List.empty[HeatupKey]
  var keyStrokesPerMinute = 300
  var heatupFactor = 1.0
  var sparkLife = 3000
  var sparkCount = 10
  var shakeRange = 4
  var flamesEnabled: Boolean = true
  var maybeElementOfPowerContainerManager =
    Option.empty[ElementOfPowerContainerManager]
  var isBamEnabled: Boolean = true
  var isSoundsPlaying = false
  var powerIndicatorEnabled = true
  var caretAction: Boolean = false
  var hotkeyWeight: Double = keyStrokesPerMinute * 0.05
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

  def isHotkeyHeatup = hotkeyHeatup

  def setHotkeyHeatup(h: Boolean): Unit = {
    hotkeyHeatup = h
  }

  def flameImageFolder = {
    if (!_isCustomFlameImages) Some(new File("fire/animated/256"))
    else customFlameImageFolder
  }

  def bamImageFolder = {
    if (!_isCustomBamImages) Some(new File("bam")) else customBamImageFolder
  }

  def getFrameRate() = frameRate

  def setFrameRate(f: Int) {
    frameRate = f
  }

  def increaseHeatup(
      dataContext: Option[DataContext] = Option.empty[DataContext],
      keyStroke: Option[KeyStroke] = Option.empty[KeyStroke]): Unit = {
    val ct = System.currentTimeMillis()
    lastKeys = (keyStroke, ct) :: filterLastKeys(ct)
    dataContext.foreach(dc =>
      maybeElementOfPowerContainerManager.foreach(_.showIndicator(dc)))

  }

  def reduceHeatup: Unit = {
    val ct = System.currentTimeMillis()
    lastKeys = filterLastKeys(ct)
    //    maybeElementOfPowerContainerManager.map(_.showIndicator)
  }

  private def filterLastKeys(ct: Long): List[HeatupKey] = {
    lastKeys.filter(_._2 >= ct - heatupTime)
  }

  def rawValueFactor = {
    val base = heatupFactor +
      ((1 - heatupFactor) * rawTimeFactor)
    val elems = (base - heatupThreshold) / (1 - heatupThreshold)
    elems
  }

  def rawTimeFactor: Double = {
    val tf = Try {
      if (heatupTime < 1000) {
        1
      } else {
        val d = heatupTime.toDouble / (60000.0 / keyStrokesPerMinute)
        val keysWorth = lastKeys.map {
          case (Some(ks), _) =>
            val size = Seq(InputEvent.CTRL_DOWN_MASK,
                           InputEvent.ALT_DOWN_MASK,
                           InputEvent.SHIFT_DOWN_MASK).count(m =>
              (ks.getModifiers & m) > 0)
            val res = size * hotkeyWeight
            res
          case _ => 1
        }.sum
        keysWorth / d
      }
    }.getOrElse(0.0)
    tf
  }

  def valueFactor: Double = {
    val base = heatupFactor +
      ((1 - heatupFactor) * timeFactor)
    val elems = (base - heatupThreshold) / (1 - heatupThreshold)

    val max = Seq(elems, 0.0).max
    assert(max <= 1)
    assert(max >= 0)
    max
  }

  def timeFactor: Double = {
    val tf = Try {
      if (heatupTime < 1000) {
        1
      } else {
        val d = heatupTime.toDouble / (60000.0 / keyStrokesPerMinute)
        val keysWorth = lastKeys.map {
          case (Some(ks), _) =>
            val size = Seq(InputEvent.CTRL_DOWN_MASK,
                           InputEvent.ALT_DOWN_MASK,
                           InputEvent.SHIFT_DOWN_MASK).count(m =>
              (ks.getModifiers & m) > 0)
            val res = size * hotkeyWeight
            res
          case _ => 1
        }.sum
        math.min(keysWorth, d) / d
      }
    }.getOrElse(0.0)
    tf
  }

  override def initComponent: Unit = {
    PowerMode.logger.debug("initComponent...")
    val editorFactory = EditorFactory.getInstance
    maybeElementOfPowerContainerManager = Some(
      new ElementOfPowerContainerManager)
    maybeElementOfPowerContainerManager.foreach(
      editorFactory.addEditorFactoryListener(_, new Disposable() {
        def dispose {}
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
    return "PowerModeII"
  }

  def getState: PowerMode = {
    return this
  }

  def loadState(state: PowerMode) {
    XmlSerializerUtil.copyBean(state, this)
  }

  def isEnabled: Boolean = {
    enabled
  }

  def setEnabled(enabled: Boolean) {
    this.enabled = enabled
  }

  def isShakeEnabled: Boolean = {
    shakeEnabled
  }

  def setShakeEnabled(shakeEnabled: Boolean) {
    this.shakeEnabled = shakeEnabled
  }

  def getSparkCount = sparkCount

  def setSparkCount(sparkCount: Int) {
    this.sparkCount = sparkCount
  }

  def getSparkLife = sparkLife

  def setSparkLife(sparkRange: Int) {
    this.sparkLife = sparkRange
  }

  def getShakeRange = shakeRange

  def setShakeRange(shakeRange: Int) {
    this.shakeRange = shakeRange
  }

  def getHeatup = (heatupFactor * 100).toInt

  def setHeatup(heatup: Int) {
    this.heatupFactor = heatup / 100.0
  }

  def getHeatupTime = heatupTime

  def setHeatupTime(heatupTime: Int) {
    this.heatupTime = math.max(0, heatupTime)
  }

  def getFlameLife: Int = {
    return maxFlameLife
  }

  def setFlameLife(flameLife: Int): Unit = {
    maxFlameLife = flameLife
  }

  def getmaxFlameSize: Int = {
    return maxFlameSize
  }

  def setmaxFlameSize(maxFlameSize: Int): Unit = {
    this.maxFlameSize = maxFlameSize
  }

  def getKeyStrokesPerMinute: Int = {
    return keyStrokesPerMinute
  }

  def setKeyStrokesPerMinute(keyStrokesPerMinute: Int) {
    this.keyStrokesPerMinute = keyStrokesPerMinute
  }

  def isFlamesEnabled: Boolean = {
    return flamesEnabled
  }

  def setFlamesEnabled(flamesEnabled: Boolean) {
    this.flamesEnabled = flamesEnabled
  }

  def isSparksEnabled: Boolean = {
    return sparksEnabled
  }

  def setSparksEnabled(sparksEnabled: Boolean) {
    this.sparksEnabled = sparksEnabled
  }

  def getSparkSize: Int = {
    return sparkSize
  }

  def setSparkSize(sparkSize: Int) {
    this.sparkSize = sparkSize
  }

  def getGravityFactor(): Double = gravityFactor

  def setGravityFactor(f: Double) {
    gravityFactor = f
  }

  def getSparkVelocityFactor(): Double = sparkVelocityFactor

  def setSparkVelocityFactor(f: Double) {
    sparkVelocityFactor = f
  }

  def getRedFrom: Int = {
    redFrom
  }

  def setRedFrom(redFrom: Int) {
    if (redFrom <= redTo)
      this.redFrom = redFrom
  }

  def getRedTo: Int = {
    return redTo
  }

  def setRedTo(redTo: Int) {
    if (redTo >= redFrom)
      this.redTo = redTo
  }

  def getGreenTo: Int = {
    return greenTo
  }

  def setGreenTo(greenTo: Int) {
    if (greenTo >= greenFrom)
      this.greenTo = greenTo
  }

  def getGreenFrom: Int = {
    return greenFrom
  }

  def setGreenFrom(gf: Int) {
    if (gf <= greenTo)
      greenFrom = gf
  }

  def getBlueTo: Int = {
    return blueTo
  }

  def setBlueTo(blueTo: Int) {
    if (blueTo >= getBlueFrom)
      this.blueTo = blueTo
  }

  def getBlueFrom: Int = {
    return blueFrom
  }

  def setBlueFrom(bf: Int) {
    if (bf <= blueTo)
      blueFrom = bf
  }

  def getColorAlpha: Int = {
    return colorAlpha
  }

  def setColorAlpha(alpha: Int) {
    colorAlpha = alpha
  }

  def getSoundsFolder = soundsFolder.map(_.getAbsolutePath).getOrElse("")

  def setSoundsFolder(file: String) {
    soundsFolder = Option(new File(file))
  }

  def getIsCaretAction: Boolean = {
    caretAction
  }

  def setIsCaretAction(isCaretAction: Boolean) {
    this.caretAction = isCaretAction
  }

  def getIsSoundsPlaying = isSoundsPlaying

  def setIsSoundsPlaying(isSoundsPlaying: Boolean) {
    this.isSoundsPlaying = isSoundsPlaying
  }

  def getBamLife = bamLife

  def setBamLife(l: Long) {
    bamLife = l
  }

  def getIsBamEnabled = isBamEnabled

  def setIsBamEnabled(b: Boolean) {
    isBamEnabled = b
  }

  def getHeatupThreshold: Int = {
    (heatupThreshold * 100.0).toInt
  }

  def setHeatupThreshold(t: Int) {
    heatupThreshold = t / 100.0
  }

  def getIsPowerIndicatorEnabled: Boolean = {
    return powerIndicatorEnabled
  }

  def setIsPowerIndicatorEnabled(enabled: Boolean) {
    powerIndicatorEnabled = enabled
  }

  def isCustomFlameImages = _isCustomFlameImages

  def setCustomFlameImages(s: Boolean) {
    _isCustomFlameImages = s
  }

  def isCustomBamImages = _isCustomBamImages

  def setCustomBamImages(s: Boolean) {
    _isCustomBamImages = s
  }

  def getCustomFlameImageFolder: String =
    customFlameImageFolder.map(_.getAbsolutePath).getOrElse("")

  def setCustomFlameImageFolder(file: String) {
    customFlameImageFolder = Option(new File(file))
  }

  def getCustomBamImageFolder =
    customBamImageFolder.map(_.getAbsolutePath).getOrElse("")

  def setCustomBamImageFolder(file: String) {
    customBamImageFolder = Option(new File(file))
  }

  def getIsSingleBamImagePerEvent() = {
    isSingleBamImagePerEvent
  }

  def setIsSingleBamImagePerEvent(isSingleBamImagePerEvent: Boolean): Unit = {
    this.isSingleBamImagePerEvent = isSingleBamImagePerEvent
  }
}
