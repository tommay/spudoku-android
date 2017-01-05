package net.tommay.sudoku

import scala.util.Random

object Create {
  val doThis = doOne(_)

  // val solveFunc = Solver.solutions SolverOptions.noGuessing
  val solveFunc = Solver.solutions(SolverOptions.all)(_)

  def main(args: Array[String]) {
    args match {
      case Array(layoutName) =>
        Layout.getLayout(layoutName) match {
          case Some(layout) => doThis(layout)
          case _ => showLayouts
        }
      case _ => showLayouts
    }
  }

  def showLayouts : Unit = {
    println("Valid layouts:")
    println(Layout.getLayoutNames.mkString(" "))
  }

  def doOne(layout: Iterable[Iterable[Int]]) = {
    val rnd = Random
    val puzzle = Creater.create(rnd, layout, solveFunc)
    println(puzzle.toPuzzleString)
  }
}

