import mill._, scalalib._

/**
 * Scala 2.12 module that is source-compatible with 2.11.
 * This is due to Chisel's use of structural types. See
 * https://github.com/freechipsproject/chisel3/issues/606
 */
trait HasXsource211 extends ScalaModule {
  override def scalacOptions = T {
    super.scalacOptions() ++ Seq(
      "-deprecation",
      "-unchecked",
      "-Xsource:2.11"
    )
  }
}

object detection extends ScalaModule with HasXsource211 {
    def scalaVersion = "2.12.10"

    def ivyDeps = Agg(
        ivy"edu.berkeley.cs::chisel3:3.3.2"
        // ivy"edu.berkeley.cs::chisel3:3.5-SNAPSHOT"
    )
}
