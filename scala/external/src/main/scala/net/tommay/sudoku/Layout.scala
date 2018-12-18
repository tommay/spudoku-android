package net.tommay.sudoku

/* The functions in this list take a position from 0 to 81 and return
   a Set of all positions that should have their colors removed
   together when creating a puzzle. */

object Layout {
  val layoutList : List[(String, Int => Set[Int])] =
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
      "tumbler" -> tumbler,
      "barcode" -> barcode,
      "random" -> random
    )

  def classic(n: Int) : Set[Int] = {
    Set(n, 80 - n)
  }

  def reflectLeftRight(n: Int) : Int = {
    val (_, col) = rowcol(n)
    n - col + 8 - col
  }

  def leftRight(n: Int) : Set[Int] = {
    Set(n, reflectLeftRight(n))
  }

  def reflectUpDown(n: Int) : Int = {
    val (row, _) = rowcol(n)
    val rowish = row * 9
    n - rowish + 72 - rowish
  }

  def leftRightUpDown(n: Int) : Set[Int] = {
    val leftRightSets = leftRight(n)
    leftRightSets ++ leftRightSets.map(reflectUpDown)
  }

  def identicalSquares(n: Int) : Set[Int] = {
    val col = n % 3
    val row = (n / 9) % 3
    val base = row*9 + col
    Set(0, 3, 6, 27, 30, 33, 54, 57, 60).map(base + _)
  }

  def quarterTurn(n: Int) : Set[Int] = {
    spinny(Set.empty, n)
  }

  def spinny(result: Set[Int], n: Int) : Set[Int] = {
    // Rotate n 90 degrees, add it, and recurse until we've come back
    // to where we started.
    if (result.contains(n)) {
      result
    }
    else {
      val (row, col) = rowcol(n)
      val rowPrime = col
      val colPrime = 8 - row
      spinny(result + n, rowPrime * 9 + colPrime)
    }
  }

  def random(n: Int) : Set[Int] = {
    Set(n)
  }

  def reflectDiagonally(n: Int) : Int = {
    val (row, col) = rowcol(n)
    val rowPrime = col
    val colPrime = row
    rowPrime * 9 + colPrime
  }

  def diagonal(n: Int) : Set[Int] = {
    Set(n, reflectDiagonally(n))
  }

  def reflectOtherDiagonally(n: Int) : Int = {
    val (row, col) = rowcol(n)
    val rowPrime = 8 - col
    val colPrime = 8 - row
    rowPrime * 9 + colPrime
  }

  def otherDiagonal(n: Int) : Set[Int] = {
    Set(n, reflectOtherDiagonally(n))
  }

  def doubleDiagonal(n: Int) : Set[Int] = {
    val diagonalSets = diagonal(n)
    diagonalSets ++ diagonalSets.map(reflectOtherDiagonally)
  }

  def kaleidoscope(n: Int) : Set[Int] = {
    quarterTurn(n) ++ quarterTurn(reflectLeftRight(n))
  }

  def tumbler(n: Int) : Set[Int] = {
    val tumbled = List(
      Set(0, 5, 26, 51, 60, 59, 74, 45),
      Set(1, 14, 25, 42, 61, 68, 73, 36),
      Set(2, 23, 24, 33, 62, 77, 72, 27),
      Set(9, 4, 17, 52, 69, 58, 65, 46),
      Set(10, 13, 16, 43, 70, 67, 64, 37),
      Set(11, 22, 15, 34, 71, 76, 63, 28),
      Set(18, 3, 8, 53, 78, 57, 56, 47),
      Set(19, 12, 7, 44, 79, 66, 55, 38),
      Set(20, 21, 6, 35, 80, 75, 54, 29),
      Set(30, 32, 50, 48),
      Set(31, 41, 49, 39),
      Set(40)
    )
    tumbled.find(_.contains(n)).getOrElse(Set.empty)
  }

  def barcode(n: Int) : Set[Int] = {
    val (row, col) = rowcol(n);
    val row0 = row - (row % 3)
    Set(0, 1, 2).map{n => (row0 + n) * 9 + col}
  }

  def getLayout(name: String) : Option[Iterable[Set[Int]]] = {
    val nameLower = name.toLowerCase
    layoutList.find(_._1.toLowerCase == name) match {
      case None => None
      case Some((_, func)) =>
        val sets = (0 to 80).map(func(_))
        Some(uniqBy(sets, {x:Set[Int] => x.min}))
    }
  }

  def getLayoutNames : Iterable[String] = {
    layoutList.map(_._1)
  }

  def rowcol(n: Int) : (Int, Int) = {
    (n / 9, n % 9)
  }

  def uniqBy[T,K](list: Iterable[T], func: T => K) : Iterable[T] = {
    list.groupBy(func)
      .values
      .map(_.head)
  }
}
