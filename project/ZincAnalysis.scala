import sbt.Keys.*
import sbt.*

import scala.collection.compat.*

object ZincAnalysis extends AutoPlugin {
  private def format[T](a: Array[T]) = a.mkString("[", ",", "]")

  private def arrayDeepEquals[T <: AnyRef](a: Array[T], b: Array[T]) = {
    java.util.Arrays.deepEquals(a.asInstanceOf[Array[AnyRef]], b.asInstanceOf[Array[AnyRef]])
  }

  override lazy val projectSettings = Seq(
    Compile / compileIncremental := {
      import sbt.internal.inc.*

      val result = (Compile / compileIncremental).value
      val oldResults = (Compile / previousCompile).value

      val analysis = result.analysis.asInstanceOf[Analysis]

      val oldAnalysis = oldResults.analysis.asScala.map(_.asInstanceOf[Analysis])

      val newCompilations = analysis.compilations.allCompilations.diff(oldAnalysis.map(_.compilations.allCompilations).getOrElse(Nil))
      println(s"Compilation cycles: ${newCompilations.size}")
      val prevCompilations = analysis.compilations.allCompilations.drop(1).zip(analysis.compilations.allCompilations).toMap

      for (compilation <- newCompilations) {
        val now = compilation.getStartTime
        val currentStamps = analysis.stamps.products.filter { stamp =>
          !stamp._2.getLastModified.filter(_ == now).isEmpty
        }
        val prevStamps = prevCompilations.get(compilation).flatMap(c => oldAnalysis.map(_.stamps))

        val recompiledClasses = analysis.apis.internal.collect {
          case (src, api) if api.compilationTimestamp() == now => src
        }.toSet

        val recompiledSources = recompiledClasses.flatMap { className =>
          analysis.relations.definesClass(className)
        }
        val recompiledSourceMap = recompiledClasses.view.flatMap { className =>
          analysis.relations.definesClass(className).map { file =>
            className -> file
          }
        }.toMap

        println(s"Recompiled sources: ${recompiledSources.size}")
        recompiledSources.foreach { s =>
          //println(s"   $s stamp ${analysis.stamps.sources.get(s)}, old ${prevStamps.flatMap(_.sources.get(s).flatMap(_.getLastModified.asScala))}")
          println(s"   $s${if (analysis.stamps.sources.get(s) != prevStamps.flatMap(_.sources.get(s))) " modified" else ""}")
        }
        println(s"Recompiled classes: ${recompiledClasses.size}")
        val classChanges = recompiledClasses.flatMap { className =>
          val newClass = analysis.apis.internal.get(className)
          val oldClass = oldAnalysis.flatMap(_.apis.internal.get(className))
          val changes = Option.when (newClass != oldClass) {
            val changes = Seq(
              Option.when(newClass.exists(n => !oldClass.exists(o => o.apiHash == n.apiHash)))("apiHash"),
              Option.when(newClass.exists(n => !oldClass.exists(o => o.nameHashes.sameElements(n.nameHashes))))("nameHashes"),
              (oldClass, newClass) match {
                case (Some(o), Some(n)) if o.api != n.api =>
                  val annotationsOC = o.api.classApi.annotations
                  val annotationsNC = n.api.classApi.annotations
                  val annotationsOO = o.api.objectApi.annotations
                  val annotationsNO = n.api.objectApi.annotations
                  val changes = Seq(
                    Option.when(o.api.classApi != n.api.classApi)(s"class api"),
                    Option.when(o.api.objectApi != n.api.objectApi)(s"object api"),
                    Option.when(!arrayDeepEquals(annotationsOC, annotationsNC))(s"class annotations: ${format(annotationsOC)} -> ${format(annotationsNC)}"),
                    Option.when(!arrayDeepEquals(annotationsOO, annotationsNO))(s"object annotations: ${format(annotationsOO)} -> ${format(annotationsNO)}")
                  ).flatten
                  if (changes.nonEmpty) {
                    Some(changes.mkString(","))
                  } else {
                    Some(s"api ${o.api.equals(n.api)} ${o.api} -> ${n.api}")
                    //Some(s"api")
                  }
                case (Some(o), None) => Some("api removed")
                case (None, Some(n)) => Some("new api")
                case _ => None
              }
            ).flatten
            if (changes.nonEmpty) changes.mkString(",") else "for unknown reasons"
          }
          changes.map(reason => className -> reason)
        }
        val sourceGroups = classChanges.groupBy(kv => recompiledSourceMap(kv._1))

        sourceGroups.foreach { case (source, classes) =>
          println(s"   $source dependencies changed:")
          classes.foreach { case (cls, reason) =>
            println(s"      $cls - $reason")
          }
        }

        recompiledClasses.foreach { src =>
          val deps = analysis.relations.a.internalClassDep.forward(src)
          //println(s"$src depends on: ${deps.mkString(", ")}")

          val dependentSources = deps.flatMap { depClassName =>
            analysis.relations.definesClass(depClassName)
          }
          //println(s"$src depends on: ${dependentSources.mkString(", ")}")

        }
      }

      result
    }
  )
}

