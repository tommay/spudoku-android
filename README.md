# Spudoku

## What is it?

Spudoku is an Android app that creates Colorku puzzles of various
difficulties and lets you solve them.

## ColorKu?  What's that?

ColorKu is like sudoku except it uses colors instead of digits.  So to
solve a ColorKu puzzle you need to figure out where to put the colors
so that each row, column, and 3x3 square contains one of each of the
nine colors.

## That's easy!

If you say so.  But Spudoku can make puzzles of various difficulties,
anywhere from EasyPeasy which is even easier than Easy up to Wicked
which is so hard that not even Spudoku knows how to solve them without
guessing.  But maybe you're smarter than Spudoku.

## Is that all?

Mostly.  But you know how sudoku puzzles always (or at least
traditionally) have that symmetry where if you rotate it a half turn
the digits that are already filled in for you end up in the same
places?  Well Spudoku has a lot more choices for its layout symmetry.
My favorite is Kaleidoscope.

## No really, is that all?

I was just getting to the good part.  If you get stuck on a puzzle and
don't know what to do, Spudoku can give you a hint.

## That's amazing!

I know right?

# Some history

### The old days.

The code was originally written in Haskell which is an amazingly
expressive and compact language.  I should have learned Haskell a long
time ago, especially since I've been a proponent of functional-style
programming and immutable data for decades.

My Haskell code had a text file interface, meaning it could create and
solve puzzles that were rendered as text files.  Nothing interactive.
As a web service coder, that was in my comfort zone.

## But then what?

That was cool, but wouldn't it be cooler if it ran on my phone and
used real colors instead of digits in text files and would let you
solve puzzles interactively?  Of course it would.

## But what about...

Are you wondering what about Android not supporting Haskell?  You're
right, it doesn't.  That's why my original thought was to redo the
logic in Java, which is pretty much Android's native
language<sup>*</sup>, and a language I'd been using a lot since the
mid-90s.  Although it's been getting a bit long in the tooth for most
of that time.

<sup>*</sup> Now Google is pushing Kotlin but I started this project in
2016.

## So you did it in Java?

No.  I got bogged down trying to figure out all the correct
incantations for typing things with generics to keep the compiler
happy.  Trying to make the compiler happy made me unhappy.  My code's
logic was getting buried in verbose (and useless) type declarations.

I forgot to mention that Haskell has type inference which makes things
pretty breezy.  Using Java was like going back to the stone age of
programming.

## You were up a creek without much of a paddle.

I sure was.  But Android can run *any* JVM language, not just Java.
In particular, it can run Frege, which is pretty much Haskell for the
JVM.

## So you used Frege?

I did.  I made some minor tweaks to my Haskell code so the Frege
compiler would be happy with it, and I wrote some code to glue it into
the UI code which was written in Java which is after all Android's
native language (Kotlin notwithstanding), with lots of documentation
and things I could crib off of stackoverflow.  Then I did all the
gradle voodoo to get the project building and voila, it was running on
Android using a colorku back end written in Frege.

## And you were done.

Yes and no.  Theoretically yes, but for practical purposes, no.  You
see, it's possible to ask Spudoku to create puzzles with certain
combinations of difficulty level and layout symmetry that may be
impossible to create.  Which would take a very long time, like
forever.  I needed a way for the user to cancel puzzle creation that
was taking too long without having to close the app.

And either I'm not a good enough functional programmer, with the
monads and all, or I'm too lazy to figure it out, but I just didn't
want to go down the path of getting Frege to cancel puzzle creation at
an arbitrary user-initiated time and return a Nothing.  Although I
probably would have learned something useful if I'd tried.

## So what did you do?

I revisited the Scala language, which I had some previous experience
with.  Scala had a lot of things going for it:

  * It's a JVM language.
  * It has lazy Streams that can be used in place of Haskell's Lists
    (did I mention that Haskell is a 100% lazy language?  Very nice.)
  * It comes with immutable/persistent data structures like Map and
    Set just like Haskell.
  * It can be programmed in a functional style with lambdas and
    pattern matching and fold functions and even currying.
  * It has type inference!  I'd be totally off the hook for
    puzzling out type declarations because the compiler can figure
	them out better than I can.
  
Because it had suitable counterparts for all the Haskell features I
was using, it was straightforward to do a Scala rewrite.  And because
it's not a pure functional language it was easy for me to cancel
puzzle creation at an arbitrary user-initiated time.

These days I have more experience with monads and could likely do it
all in Frege.

## What about Kotlin?

What about it?  You tell me.

## No, I meant what about you being a web service coder and not a UI guy?

Oh.  Yeah.  Well.  I did have to learn a thing or two about widgets
and onClick handlers but really the things you do to keep an
interactive app responsive are the same things we do to keep a web
service responsive.  The worst part was dealing with layout managers
which even when you have as few widgets as Spudoku does can seem
pretty hit or miss as you try to coax the layout manager into putting
things where you want them only to find it goes berserk when you go
into multi-window mode and back.

## But Android Studio has a visual layout editor.

And it has a ton of confusing visual clutter for things I don't want
or need taking up my valuable screen real estate, things I can't
really see anyway because I have low vision.  Give me an editor and a
command line any day over an IDE.  I did try to use the layout editor
but it updated a bunch of stuff in my project that broke the build.

If you're a happy Android Studio user that's fine, I still like you,
it just doesn't work for *me*.

## Is that why your build instructions say how to set up and build
with command line tools?

That's why.

## Did you say the code can solve puzzles in addition to creating them?

Yes, it can.  That's how it gives hints.  It finds the simplest way to
solve the next step of a puzzle then gives you a hint about how to do
it.

## How does it create puzzles?

In fact, solving puzzles is actually how it creates them.  It starts
with a completely blank puzzle and solves it, which gives a nearly
infinite Stream of all possible solved sudoku puzzles in a random
order.  Then it takes the first one.  Since the Stream is lazy, only
the first one ever actually gets computed.

Next it removes colors from the solution in sets of positions that
will leave the requested layout.  Each time it removes a set it solves
the resulting puzzle and if there is still exactly one solution it
iterates to try removing more colors.

To make puzzles of a particular difficulty it tries to solve the
resulting puzzle with a particular set of heuristics.  If the puzzle
can be solved, it is done.  Otherwise it moves on to the next solved
puzzle in the nearly infinite Stream and the whole thing starts over.

Except what is really happening is that it takes the Stream of created
puzzles, filters it for puzzles that can be solved with a particular
set of heuristics, and returns the first one.  Lazy functional
programming is cool.

I'm not describing that very well.

## Build instructions

These build instructions use command line tools only.  If you want to
import the project into Android Studio then let's assume you know what
you're doing and have at it.

Also, these instructions are for Arch Linux running in VirtualBox
because that's *my* build system and I figure I'm the one most likely
to be following these instructions to set up a new build some day.

### No, really

I really do have build instructions written up.  Otherwise I wouldn't
be able to build this myself on a new system.

I just have to get them copied and formatted into this README.
