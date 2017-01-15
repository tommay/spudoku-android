package net.tommay.spudoku;

/** Get layout names from the frege code. */

import java.util.LinkedList;
import java.util.List;

import net.tommay.sudoku.Layout;

import frege.prelude.PreludeBase;
import frege.prelude.PreludeBase.TList;

class LayoutNames {
    public static List<String> getLayoutNames () {
        List<String> layoutNames = new LinkedList();

        TList<String> list = Layout.getLayoutStrings.call();
        TList.DCons<String> dcons;
        while ((dcons = list.asCons()) != null) {
            layoutNames.add(dcons.mem1.call());
            list = dcons.mem2.call();
        }

        return layoutNames;
    }
}

/*
PreludeList.java:1343  Walks a list to get the length:

 final public static <α> int length(final PreludeBase.TList<α> arg$1) {
    final class Let$4278  {
      final Let$4278 let$4278 = this;
      final public int len$2721(PreludeBase.TList<α> arg$2, int arg$3) {
        tailrecursion: while (true) {
          final PreludeBase.TList<α> arg$2f = arg$2;
          final int arg$3f = arg$3;
          final PreludeBase.TList.DCons<α> $4279 = arg$2f.asCons();
          if ($4279 != null) {
            final PreludeBase.TList<α> p$2969$2969 = $4279.mem2.call();
            arg$2 = p$2969$2969;
            arg$3 = arg$3f + 1;
            continue tailrecursion;
          }
          final PreludeBase.TList.DList<α> $4281 = arg$2f.asList();
          assert $4281 != null;
          return arg$3f;
        }
      }
    }
    final Let$4278 let$4278 = new Let$4278();
    return let$4278.len$2721(arg$1, 0);
  }
*/

/*
Note that out TList above is a DList implements TList.

public static abstract class TList<𝓐> implements frege.runtime.Value, Lazy<TList<𝓐>>, Kind.U<TList<?>, 𝓐> {
  final public TList<𝓐> call() {
    return this;
  }
  final public boolean isShared() {
    return true;
  }
  final public Thunk<TList<𝓐>> asThunk() {
    return null;
  }
  public TList.DList<𝓐> asList() {
    return null;
  }
  public TList.DCons<𝓐> asCons() {
    return null;
  }
  final public static class DList<𝓐> extends TList<𝓐>  {
    public TList.DList<𝓐> asList() {
      return this;
    }
    private DList() {}
    final public int constructor() {
      return 0;
    }
    private static TList<?> it = new TList.DList<Object>();
    @SuppressWarnings("unchecked") final public static <𝓐> TList<𝓐> mk() {
      return (TList<𝓐>)it;
    }
  }
  final public static class DCons<𝓐> extends TList<𝓐>  {
    public TList.DCons<𝓐> asCons() {
      return this;
    }
    private DCons(final Lazy<𝓐> arg$1, final Lazy<TList<𝓐>> arg$2) {
      mem1 = Thunk.<𝓐>shared(arg$1);
      mem2 = Thunk.<TList<𝓐>>shared(arg$2);
    }
    final public int constructor() {
      return 1;
    }
    final public static <𝓐> TList<𝓐> mk(final Lazy<𝓐> arg$1, final Lazy<TList<𝓐>> arg$2) {
      return new TList.DCons<𝓐>(arg$1, arg$2);
    }
    final public Lazy<𝓐> mem1  ;
    final public Lazy<TList<𝓐>> mem2  ;
  }
  @SuppressWarnings("unchecked") final public <α> TList<α> simsalabim() {
    return (TList<α>)this;
  }
  @SuppressWarnings("unchecked") final public static <𝓐> TList<𝓐> coerce(final Kind.U<TList<?>, 𝓐> it) {
    return (TList<𝓐>)it;
  }
}
*/
