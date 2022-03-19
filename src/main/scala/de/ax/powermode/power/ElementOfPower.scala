package de.ax.powermode.power

import de.ax.powermode.Power

import java.awt.Graphics

/**
  * Created by nyxos on 10.03.16.
  */
trait ElementOfPower extends Power {

  def update(delta: Float): Boolean

  def render(g: Graphics, dxx: Int, dyy: Int): Unit

  def life: Long

  def initLife: Long

  def lifeFactor: Float = {
    1 - ((life - System.currentTimeMillis()) / initLife.toFloat)
  }

  def alive: Boolean = {
    life > System
      .currentTimeMillis() && powerMode.isEnabled //&& width >= 0 && width <= _width
  }
}
