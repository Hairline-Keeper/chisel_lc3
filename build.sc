import os.Path
import mill._
import scalalib._

/**
 * Scala 2.12 module that is source-compatible with 2.11.
 * This is due to Chisel's use of structural types. See
 * https://github.com/freechipsproject/chisel3/issues/606
 */

object ivys {
  val sv = "2.12.13"
  val chisel3 = ivy"edu.berkeley.cs::chisel3:3.5.0-RC1"
  val chisel3Plugin = ivy"edu.berkeley.cs:::chisel3-plugin:3.5.0-RC1"
  val chiseltest = ivy"edu.berkeley.cs::chiseltest:0.3.2"
  val scalatest = ivy"org.scalatest::scalatest:3.2.2"
  val macroParadise = ivy"org.scalamacros:::paradise:2.1.1"
}

trait LC3Module extends ScalaModule {
  override def scalaVersion = ivys.sv
  override def ivyDeps = Agg(ivys.chisel3, ivys.chiseltest)
  override def compileIvyDeps = Agg(ivys.macroParadise)
  override def scalacPluginIvyDeps = Agg(ivys.macroParadise, ivys.chisel3Plugin)
  override def scalacOptions = Seq("-Xsource:2.11")
}

object chisel_lc3 extends LC3Module with SbtModule {
  override def millSourcePath = os.pwd
  object test extends Tests with TestModule.ScalaTest {
    override def ivyDeps = super.ivyDeps() ++ Agg(
      ivys.scalatest
    )
  }
  
}
