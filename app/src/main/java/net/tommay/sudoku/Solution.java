/*
  Source code is in UTF-8 encoding. The following symbols may appear, among others:
  α β γ δ ε ζ η θ ι κ λ μ ν ξ ο π ρ ς σ τ υ φ χ ψ ω « • ¦ » ∀ ∃ ∷ … → ← ﬁ ﬂ ƒ
  If you can't read this, you're out of luck. This code was generated with the frege compiler version 3.24.141
  from ./net/tommay/sudoku/Solution.fr Do not edit this file! Instead, edit the source file and recompile.
*/

package net.tommay.sudoku;

import frege.run7.Func;
import frege.run7.Lazy;
import frege.run7.Thunk;
import frege.run.Kind;
import frege.run.RunTM;
import frege.runtime.Meta;
import frege.runtime.Phantom.RealWorld;
import frege.Prelude;
import frege.control.Category;
import frege.control.Semigroupoid;
import frege.data.Bits;
import frege.data.Char;
import frege.data.Foldable;
import frege.data.HashMap;
import frege.data.Iterators;
import frege.data.JSON;
import frege.data.List;
import frege.data.MicroParsec;
import frege.data.Monoid;
import frege.data.Traversable;
import frege.data.wrapper.Const;
import frege.data.wrapper.Dual;
import frege.data.wrapper.Endo;
import frege.data.wrapper.Identity;
import frege.data.wrapper.Num;
import frege.java.IO;
import frege.java.Lang;
import frege.java.Util;
import frege.java.util.Regex;
import frege.lib.PP;
import frege.prelude.Maybe;
import frege.prelude.PreludeArrays;
import frege.prelude.PreludeBase;
import frege.prelude.PreludeIO;
import frege.prelude.PreludeList;
import frege.prelude.PreludeMonad;
import frege.prelude.PreludeText;
import frege.system.Random;
import net.tommay.sudoku.Digit;
import net.tommay.sudoku.Placement;
import net.tommay.sudoku.Puzzle;
import net.tommay.sudoku.Step;

@SuppressWarnings("unused")
@Meta.FregePackage(
  source="./net/tommay/sudoku/Solution.fr", time=1481937171703L, jmajor=1, jminor=7,
  imps={
    "frege.Prelude", "frege.prelude.PreludeArrays", "frege.prelude.PreludeBase", "frege.prelude.PreludeIO",
    "frege.prelude.PreludeList", "frege.prelude.PreludeMonad", "frege.prelude.PreludeText", "net.tommay.sudoku.Puzzle",
    "frege.java.util.Regex", "net.tommay.sudoku.Step"
  },
  nmss={
    "Prelude", "PreludeArrays", "PreludeBase", "PreludeIO", "PreludeList", "PreludeMonad", "PreludeText",
    "Puzzle", "Regexp", "Step"
  },
  symas={}, symcs={}, symis={},
  symts={
    @Meta.SymT(
      offset=134, name=@Meta.QName(kind=0, pack="net.tommay.sudoku.Solution", base="Solution"), typ=0,
      kind=5,
      cons={
        @Meta.SymD(
          offset=145, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="Solution"),
          cid=0, typ=3,
          fields={
            @Meta.Field(name="puzzle", offset=158, sigma=1, strict=false),
            @Meta.Field(name="steps", offset=178, sigma=2, strict=false)
          },
          priv=true, publik=false
        )
      },
      lnks={},
      funs={
        @Meta.SymV(
          offset=179, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="upd$steps"),
          stri="s(su)", sig=4, depth=2, rkind=49, doc="update field @steps@"
        ),
        @Meta.SymV(
          offset=179, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="steps"),
          stri="s(s)", sig=5, depth=1, rkind=49, doc="access field @steps@"
        ),
        @Meta.SymV(
          offset=159, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="upd$puzzle"),
          stri="s(su)", sig=6, depth=2, rkind=49, doc="update field @puzzle@"
        ),
        @Meta.SymV(
          offset=179, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="has$steps"),
          stri="s(u)", sig=8, depth=1, rkind=49, doc="check if constructor has field @steps@"
        ),
        @Meta.SymV(
          offset=179, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="chg$steps"),
          stri="s(ss)", sig=10, depth=2, rkind=49, doc="change field @steps@"
        ),
        @Meta.SymV(
          offset=159, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="has$puzzle"),
          stri="s(u)", sig=8, depth=1, rkind=49, doc="check if constructor has field @puzzle@"
        ),
        @Meta.SymV(
          offset=159, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="chg$puzzle"),
          stri="s(ss)", sig=12, depth=2, rkind=49, doc="change field @puzzle@"
        ),
        @Meta.SymV(
          offset=159, name=@Meta.QName(kind=2, pack="net.tommay.sudoku.Solution", base="Solution", member="puzzle"),
          stri="s(s)", sig=13, depth=1, rkind=49, doc="access field @puzzle@"
        )
      },
      prod=true
    )
  },
  symvs={
    @Meta.SymV(
      offset=197, name=@Meta.QName(pack="net.tommay.sudoku.Solution", base="new"), stri="s(uu)",
      sig=3, depth=2, rkind=49
    )
  },
  symls={},
  taus={
    @Meta.Tau(kind=2, suba=0, tcon={@Meta.QName(kind=0, pack="net.tommay.sudoku.Solution", base="Solution")}),
    @Meta.Tau(kind=2, suba=0, tcon={@Meta.QName(kind=0, pack="net.tommay.sudoku.Puzzle", base="Puzzle")}),
    @Meta.Tau(kind=2, suba=0, tcon={@Meta.QName(kind=0, pack="frege.prelude.PreludeBase", base="[]")}),
    @Meta.Tau(kind=2, suba=0, tcon={@Meta.QName(kind=0, pack="net.tommay.sudoku.Step", base="Step")}),
    @Meta.Tau(kind=0, suba=2, subb=3), @Meta.Tau(kind=9), @Meta.Tau(suba=5, tvar="α"),
    @Meta.Tau(kind=2, suba=0, tcon={@Meta.QName(kind=0, pack="frege.prelude.PreludeBase", base="Bool")}),
    @Meta.Tau(kind=2, suba=0, tcon={@Meta.QName(kind=0, pack="frege.prelude.PreludeBase", base="->")}),
    @Meta.Tau(kind=0, suba=8, subb=4), @Meta.Tau(kind=0, suba=9, subb=4), @Meta.Tau(kind=0, suba=8, subb=1),
    @Meta.Tau(kind=0, suba=11, subb=1)
  },
  rhos={
    @Meta.Rho(rhofun=false, rhotau=0), @Meta.Rho(rhofun=false, rhotau=1), @Meta.Rho(rhofun=false, rhotau=4),
    @Meta.Rho(sigma=2, rhotau=0), @Meta.Rho(sigma=1, rhotau=3), @Meta.Rho(sigma=0, rhotau=3),
    @Meta.Rho(sigma=0, rhotau=2), @Meta.Rho(sigma=1, rhotau=0), @Meta.Rho(sigma=0, rhotau=7),
    @Meta.Rho(rhofun=false, rhotau=6), @Meta.Rho(rhofun=false, rhotau=7), @Meta.Rho(sigma=7, rhotau=10),
    @Meta.Rho(rhofun=false, rhotau=10), @Meta.Rho(sigma=9, rhotau=0), @Meta.Rho(sigma=0, rhotau=13),
    @Meta.Rho(rhofun=false, rhotau=12), @Meta.Rho(sigma=11, rhotau=0), @Meta.Rho(sigma=0, rhotau=16),
    @Meta.Rho(sigma=0, rhotau=1)
  },
  sigmas={
    @Meta.Sigma(rho=0), @Meta.Sigma(rho=1), @Meta.Sigma(rho=2), @Meta.Sigma(rho=4), @Meta.Sigma(rho=5),
    @Meta.Sigma(rho=6), @Meta.Sigma(rho=8), @Meta.Sigma(rho=9), @Meta.Sigma(bound={"α"}, kinds={5}, rho=11),
    @Meta.Sigma(rho=12), @Meta.Sigma(rho=14), @Meta.Sigma(rho=15), @Meta.Sigma(rho=17), @Meta.Sigma(rho=18)
  },
  exprs={@Meta.Expr()}
)
final public class Solution  {
  



final public static class TSolution implements frege.runtime.Value, Lazy<TSolution> {
  private TSolution(final Lazy<Puzzle.TPuzzle> arg$1, final Lazy<PreludeBase.TList<Step.TStep>> arg$2) {
    mem$puzzle = Thunk.<Puzzle.TPuzzle>shared(arg$1);
    mem$steps = Thunk.<PreludeBase.TList<Step.TStep>>shared(arg$2);
  }
  final public int constructor() {
    return 0;
  }
  final public static TSolution mk(final Lazy<Puzzle.TPuzzle> arg$1, final Lazy<PreludeBase.TList<Step.TStep>> arg$2) {
    return new TSolution(arg$1, arg$2);
  }
  final public Lazy<Puzzle.TPuzzle> mem$puzzle  ;
  final public Lazy<PreludeBase.TList<Step.TStep>> mem$steps  ;
  final public TSolution call() {
    return this;
  }
  final public boolean isShared() {
    return true;
  }
  final public Thunk<TSolution> asThunk() {
    return null;
  }
  @SuppressWarnings("unchecked") final public TSolution simsalabim() {
    return (TSolution)this;
  }
  final public static TSolution upd$steps(final TSolution arg$1, final Lazy<PreludeBase.TList<Step.TStep>> arg$2) {
    return TSolution.mk(arg$1.mem$puzzle, arg$2);
  }
  final public static PreludeBase.TList<Step.TStep> steps(final TSolution arg$1) {
    final PreludeBase.TList<Step.TStep> a2$9905 = arg$1.mem$steps.call();
    return a2$9905;
  }
  final public static TSolution upd$puzzle(final TSolution arg$1, final Lazy<Puzzle.TPuzzle> arg$2) {
    return TSolution.mk(arg$2, arg$1.mem$steps);
  }
  final public static <α> boolean has$steps(final Lazy<α> arg$1) {
    return true;
  }
  final public static TSolution chg$steps(
    final TSolution arg$1, final Func.U<PreludeBase.TList<Step.TStep>, PreludeBase.TList<Step.TStep>> arg$2
  ) {
    return TSolution.mk(
              arg$1.mem$puzzle,
              Thunk.<PreludeBase.TList<Step.TStep>>nested(
                    new Lazy.D<Lazy<PreludeBase.TList<Step.TStep>>>() {
                      public Lazy<PreludeBase.TList<Step.TStep>> call() {
                        return arg$2.apply(arg$1.mem$steps);
                      }
                    }
                  )
            );
  }
  final public static <α> boolean has$puzzle(final Lazy<α> arg$1) {
    return true;
  }
  final public static TSolution chg$puzzle(final TSolution arg$1, final Func.U<Puzzle.TPuzzle, Puzzle.TPuzzle> arg$2) {
    return TSolution.mk(
              Thunk.<Puzzle.TPuzzle>nested(
                    new Lazy.D<Lazy<Puzzle.TPuzzle>>() {
                      public Lazy<Puzzle.TPuzzle> call() {
                        return arg$2.apply(arg$1.mem$puzzle);
                      }
                    }
                  ),
              arg$1.mem$steps
            );
  }
  final public static Puzzle.TPuzzle puzzle(final TSolution arg$1) {
    final Puzzle.TPuzzle a1$9892 = arg$1.mem$puzzle.call();
    return a1$9892;
  }
}
final public static TSolution $new(final Lazy<Puzzle.TPuzzle> arg$1, final Lazy<PreludeBase.TList<Step.TStep>> arg$2) {
  return TSolution.mk(arg$1, arg$2);
}

}
