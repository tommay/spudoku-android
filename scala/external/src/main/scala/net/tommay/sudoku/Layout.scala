package net.tommay.sudoku

/* The functions in this list take a position from 0 to 81 and return
   a List (actually an Iterable) of all positions that should have
   their colors removed together when creating a puzzle.  */

object Layout {
  val layoutList : List[(String, Int => Iterable[Int])] =
    List(
      "classic (half turn)" -> classic,
      "quarter turn" -> quarterTurn,
      "mirror" -> leftRight,
      "double mirror" -> leftRightUpDown,
      "diagonal" -> diagonal,
      "other diagonal" -> otherDiagonal,
      "double diagonal" -> doubleDiagonal,
      "kaleidoscope" -> kaleidoscope,
      "identical squares" -> identicalSquares,
      "random" -> random,
      "wtf" -> wtf,
      "wtf2" -> wtf
    )

  def classic(n: Int) : Iterable[Int] = {
    List(n, 80 - n)
  }

  def reflectLeftRight(n: Int) : Int = {
    val (_, col) = rowcol(n)
    n - col + 8 - col
  }

  def leftRight(n: Int) : Iterable[Int] = {
    List(n, reflectLeftRight(n))
  }

  def reflectUpDown(n: Int) : Int = {
    val (row, _) = rowcol(n)
    val rowish = row * 9
    n - rowish + 72 - rowish
  }

  def leftRightUpDown(n: Int) : Iterable[Int] = {
    val leftRightSets = leftRight(n)
    leftRightSets ++ leftRightSets.map(reflectUpDown)
  }

  def identicalSquares(n: Int) : Iterable[Int] = {
    val col = n % 3
    val row = (n / 9) % 3
    val base = row*9 + col
    List(0, 3, 6, 27, 30, 33, 54, 57, 60).map(base + _)
  }

  def quarterTurn(n: Int) : Iterable[Int] = {
    spinny(List.empty, n)
  }

  def spinny(result: List[Int], n: Int) : Iterable[Int] = {
    // Rotate n 90 degrees, cons it, and recurse until we've come back
    // to where we started.
    if (result.contains(n)) {
      result
    }
    else {
      val (row, col) = rowcol(n)
      val rowPrime = col
      val colPrime = 8 - row
      spinny(n :: result, rowPrime * 9 + colPrime)
    }
  }

  def random(n: Int) : Iterable[Int] = {
    List(n)
  }

  def reflectDiagonally(n: Int) : Int = {
    val (row, col) = rowcol(n)
    val rowPrime = col
    val colPrime = row
    rowPrime * 9 + colPrime
  }

  def diagonal(n: Int) : Iterable[Int] = {
    List(n, reflectDiagonally(n))
  }

  def reflectOtherDiagonally(n: Int) : Int = {
    val (row, col) = rowcol(n)
    val rowPrime = 8 - col
    val colPrime = 8 - row
    rowPrime * 9 + colPrime
  }

  def otherDiagonal(n: Int) : Iterable[Int] = {
    List(n, reflectOtherDiagonally(n))
  }

  def doubleDiagonal(n: Int) : Iterable[Int] = {
    val diagonalSets = diagonal(n)
    diagonalSets ++ diagonalSets.map(reflectOtherDiagonally)
  }

  def kaleidoscope(n: Int) : Iterable[Int] = {
    val (row, col) = rowcol(n)
    val bigRow = row / 3
    val bigCol = col / 3

    if (bigRow == 1 && bigCol == 1) {
      // Center big square.
      quarterTurn(n)
    }
    if (bigRow == bigCol) {
      // In one of the three big squares on the NW/SE diagonal
      kaleid(reflectDiagonally, n)
    }
    else if (bigRow + bigCol == 2) {
      // In one of the three big squares on the SW/BE diagonal
      kaleid(reflectOtherDiagonally, n)
    }
    else if (bigRow == 1) {
      // Left or right edge.
      kaleid(reflectUpDown, n)
    }
    else {
      // Top or bottom edge.
      kaleid(reflectLeftRight, n)
    }
  }

  def kaleid(reflectFunc : Int => Int, n : Int) : Iterable[Int] = {
    val nPrime = reflectFunc(n)
    quarterTurn(n) ++ quarterTurn(nPrime)
  }

  def wtf(n: Int) : Iterable[Int] = {
    val (row, col) = rowcol(n)
    val rowPrime = 8 - col
    val colPrime = row
    List(n, rowPrime * 9 + colPrime)
  }

  def wtf2(n: Int) : Iterable[Int] = {
    val (row, col) = rowcol(80 - n)
    val rowPrime = col
    val colPrime = row
    List(n, rowPrime * 9 + colPrime)
  }

  def getLayout(name: String) : Option[Iterable[Iterable[Int]]] = {
    val layoutMap = layoutList
      .map{case (name, func) => (name.toLowerCase, func)}
      .toMap
    layoutMap.get(name.toLowerCase) match {
      case None => None
      case Some(func) =>
        val sets = (0 to 80).map(func(_))
        Some(uniqBy(sets, {x:Iterable[Int] => x.toList.min}))
    }
  }

  def getLayoutNames : Iterable[String] = {
    layoutList.map(_._1)
  }

  def rowcol(n: Int) : (Int, Int) = {
    (n / 9, n % 9)
  }

  def uniq[T](list: Iterable[T]): Iterable[T] = {
    list.toSet
  }

  def uniqBy[T,K](list: Iterable[T], func: T => K) : Iterable[T] = {
    list.groupBy(func)
      .values
      .map(_.head)
  }
}
