package de.ax.powermode

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

import java.util
object PowerModeStartup {
  def start(): Unit = synchronized {
    println(s"starting instance on out ${System.out.hashCode()}")
    val x = PowerMode.getInstance
    println(s"started instance ${x.hashCode()}")
  }
}
class PowerModeStartup extends com.intellij.ide.AppLifecycleListener {
  override def appFrameCreated(commandLineArgs: util.List[String]): Unit = {
    PowerModeStartup.start()
  }
}
