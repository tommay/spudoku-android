package net.tommay.sudoku

import scala.util.Random

object Util {
  // Slice an list up into sub-lists of n elements, and return the
  // sub-lists in a list.

  def slices[T](n: Int, list: Iterable[T]) : List[Iterable[T]] = {
    list.grouped(n).toList
  }

  // Weird generic voodoo as per
  // http://stackoverflow.com/questions/19385235/how-to-paramaterize-int-as-ordered-in-scala
  //
  def minBy[T, K <% Ordered[K]](list: Iterable[T], func: T => K) : T = {
    val enhanced = list.map(e => (func(e), e))
    val minEnhanced = enhanced.tail.foldLeft(enhanced.head) {
      case (a@(na, _), b@(nb, _)) =>
        // xxx choose a or b on ==?
        if (na < nb) a else b
    }
    minEnhanced._2
  }

  def shuffle[T](list: Iterable[T], rnd: Random) : List[T] = {
    rnd.shuffle(list.toList)
  }

  def split(rnd: Random) : (Random, Random) = {
    (rnd, rnd)
  }
}
