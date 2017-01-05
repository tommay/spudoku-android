This is the code to create sudoku puzzles, originally written in
Haskell and ported to functional-style Scala because the port to Java
quickly got bogged down in figuring out the correct generics.

It's integrated into the Spudoku build but the jar can be built
standalone with "gradle jar" or just use the one built by the Spudoku
build.

The code can be run with the "create" and "solve" scripts.  This
requires the "scala" command to run scala 2.11, because that's what
the jar is built with.

An alternative would be to build in uberjar with scala classes baked
in.
