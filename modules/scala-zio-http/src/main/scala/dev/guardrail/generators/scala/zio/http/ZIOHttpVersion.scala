package dev.guardrail.generators.scala.http4s

sealed abstract class ZIOHttpVersion(val value: String)
object ZIOHttpVersion {
  case object V0_22 extends ZIOHttpVersion("http4s-v0.22")
  case object V0_23 extends ZIOHttpVersion("http4s-v0.23")

  val mapping: Map[String, ZIOHttpVersion] = Map(
    "http4s"    -> V0_23,
    V0_22.value -> V0_22,
    V0_23.value -> V0_23
  )
}
