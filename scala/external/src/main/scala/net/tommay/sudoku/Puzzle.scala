package net.tommay.sudoku

// cellNumber -> Digit

// XXX this should be private, all construction via Puzzle.methods.

// All a Puzzle has is a Map from cellNumbers to the Digit placed at
// that cellNumber.

case class Puzzle(
  placed : Map[Int, Int] = Map.empty)
{
  type Digit = Int

  // For when we need to iterate over all cellNumber/Digits.

  def each: Iterable[(Int, Digit)] = {
    placed
  }

  // Adds a new Digit to the puzzle at the given cellNumber.

  def place(cellNumber: Int, digit: Digit) : Puzzle = {
    this.copy(placed = placed + (cellNumber -> digit))
  }

  // Given an Iterable of cellNumbers, removes the Digits from those
  // cellNumbers.

  def remove(cellNumbers: Iterable[Int]) : Puzzle = {
    val remaining = cellNumbers.foldLeft(placed) {
      (map, cellNumber) => map - cellNumber
    }
    this.copy(placed = remaining)
  }

  // Returns the number of placed digits.

  def size : Int = {
    placed.size
  }

  // The opposite of fromString.  Given a Puzzle, create a string of
  // 81 digits or dashes.  Creates two lists of (cellNumber, Char),
  // one for placed cells and one for unplaced cells, then sorts them
  // together and extracts the Chars in order.

  override
  def toString : String = {
    val p = each.map{
      case (k, v) => (k, (v + '0'.toInt).toChar)
    }
    val unknownNumbers = (0 to 80).toSet -- placed.keySet
    val u = unknownNumbers.map{(_, '-')}
    (p ++ u).toList.sorted.map{_._2}.mkString
  }

  // Returns a string that prints out as a grid of digits.

  def toPuzzleString : String = {
    val string = this.toString
    Util.slices(27, string).map {superRow =>
      Util.slices(9, superRow).map {row =>
        Util.slices(3, row).map{_.mkString}.mkString(" ")
      }.mkString("\n")
    }.mkString("\n\n")
  }
}

object Puzzle {
  // Returns a new Puzzle with nothing placed.

  def empty : Puzzle = {
    Puzzle()
  }

  // Returns a new Puzzle with each Cell initialized according to
  // Setup, which is a string of 81 digits or dashes.

  def fromString(setup: String) : Puzzle = {
    val digits = toDigits(setup)
    val zipped = (0 to 80).zip(digits)
    zipped.foldLeft(Puzzle.empty) {case (puzzle, (cellNumber, digit)) =>
      digit match {
        case None => puzzle
        case Some(digit) => puzzle.place(cellNumber, digit)
      }
    }
  }

  // Given a Setup String, returns a Seq of Option[Digit] for each
  // cell.

  // xxx Digit, digit conversion
  def toDigits(setup: String) : Seq[Option[Int]] = {
    setup.map{
      case '-' => None
      case char => Some(char.toInt - '0'.toInt)
    }
  }
}
