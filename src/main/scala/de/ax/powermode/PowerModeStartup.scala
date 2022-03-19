package de.ax.powermode

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
object PowerModeStartup {
  def start(): Unit = System.out.synchronized {
    println(s"starting instance on out ${System.out.hashCode()}")
    val x = PowerMode.getInstance
    println(s"started instance ${x.hashCode()}")
  }
}
class PowerModeStartup extends StartupActivity {
  override def runActivity(project: Project): Unit = synchronized {
    PowerModeStartup.start()
  }
}
