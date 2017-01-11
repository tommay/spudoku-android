# sudoku-in-haskell
I'm learning a bit about Haskell by rewriting my erlang sudoku solver in it.

Now that I've done this, I understand maybe 10% of Haskell.

I've now written it from scratch, "thinking in Haskell", which isn't
so hard after Lisp and Erlang, and using a different and much faster
data structure which just maintains lists of Placed and Unknown cells
instead of using a Vector of values that can be one or the other.  The
old version is in branch "old".  RIP, but thanks for the lessons.
