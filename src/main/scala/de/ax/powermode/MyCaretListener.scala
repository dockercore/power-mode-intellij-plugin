package de.ax.powermode

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.event.{CaretEvent, CaretListener}

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.IntUnaryOperator
import scala.util.Try

/**
  * Created by nyxos on 04.01.17.
  */
class MyCaretListener extends CaretListener with Power {
  var modified = true

  val caretCount = new AtomicInteger(1)
  override def caretPositionChanged(caretEvent: CaretEvent): Unit = {
    if (!modified && powerMode.caretAction) {
      initializeAnimationByCaretEvent(caretEvent.getCaret)
    }
    modified = false
  }

  override def caretRemoved(caretEvent: CaretEvent): Unit = {
    modified = true
    caretCount.getAndUpdate((i: Int) => {
      if (i > 2) {
        i - 1
      } else {
        1
      }
    })
  }

  override def caretAdded(caretEvent: CaretEvent): Unit = {
    modified = true
    caretCount.getAndIncrement()
  }

  private def initializeAnimationByCaretEvent(caret: Caret): Unit = {
    val isActualEditor = Try {
      Util.isActualEditor(caret.getEditor)
    }.getOrElse(false)
    if (isActualEditor) {
      Util
        .getCaretPosition(caret)
        .toOption
        .foreach(p =>
          powerMode.maybeElementOfPowerContainerManager.foreach(
            _.initializeAnimation(caret.getEditor, p)))
    }
  }
}
