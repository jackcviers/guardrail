package tests.core.issues

import cats.data.NonEmptyList
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.ParseOptions
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.meta._

import support.SwaggerSpecRunner

import dev.guardrail._
import dev.guardrail.core.Tracker
import dev.guardrail.generators.ProtocolDefinitions
import dev.guardrail.generators.scala.ScalaLanguage
import dev.guardrail.generators.scala.ScalaGeneratorMappings.scalaInterpreter
import dev.guardrail.generators.scala.zio.http.ZIOHttpVersion
import dev.guardrail.terms.protocol.ClassDefinition

class Issue166 extends AnyFunSuite with Matchers with SwaggerSpecRunner {

  val swagger = s"""
                   |swagger: "2.0"
                   |info:
                   |  title: Whatever
                   |  version: 1.0.0
                   |host: localhost:1234
                   |schemes:
                   |  - http
                   |paths:
                   |  /blix:
                   |    get:
                   |      operationId: getBlix
                   |      responses:
                   |        200:
                   |          schema:
                   |            $$ref: "#/definitions/Blix"
                   |definitions:
                   |  Blix:
                   |    type: object
                   |    required:
                   |      - map
                   |    properties:
                   |      map:
                   |        type: string
                   |""".stripMargin

  def testVersion(version: ZIOHttpVersion): Unit =
    test(s"$version - Handle generation of models") {
      val opts = new ParseOptions()
      opts.setResolve(true)

      val http4sFramework = Target.unsafeExtract(scalaInterpreter.extractGenerator(Context.empty.copy(framework = Some("http4s")), None))
      import http4sFramework._

      val (proto, codegen) = Target.unsafeExtract(
        Common
          .prepareDefinitions[ScalaLanguage, Target](
            CodegenTarget.Models,
            Context.empty,
            Tracker(new OpenAPIParser().readContents(swagger, new java.util.LinkedList(), opts).getOpenAPI),
            List.empty,
            NonEmptyList.one("support")
          )
      )

      val ProtocolDefinitions(ClassDefinition(_, _, _, cls, _, _) :: Nil, _, _, _, _) = proto
      val CodegenDefinitions(Nil, Nil, Nil, None)                                     = codegen

      val definition = q"""
      case class Blix(map: String)
    """

      cls.structure should equal(definition.structure)
    }

  testVersion(ZIOHttpVersion.V0_22)
  testVersion(ZIOHttpVersion.V0_23)
}
