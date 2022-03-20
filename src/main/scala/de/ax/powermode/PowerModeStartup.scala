package de.ax.powermode

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

import java.util
object PowerModeStartup {
  def start(): Unit = synchronized {
    PowerMode.logger.debug(s"starting instance on out ${System.out.hashCode()}")
    val x = PowerMode.getInstance
    PowerMode.logger.debug(s"started instance ${x.hashCode()}")
  }
}
class PowerModeStartup extends com.intellij.ide.AppLifecycleListener {
  override def appFrameCreated(commandLineArgs: util.List[String]): Unit = {
    PowerModeStartup.start()
  }
}
