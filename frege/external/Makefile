ALL = Solve Create Pattern # Color Uncolor Min

PACKAGE = net/tommay/sudoku

all: $(ALL)

$(ALL): FORCE
FORCE:

Solve: build/$(PACKAGE)/Solve.class
Create: build/$(PACKAGE)/Create.class
Pattern: build/$(PACKAGE)/Pattern.class

main: build/$(PACKAGE)/Main.class
build/$(PACKAGE)/Main.class: Main.java CreaterForJava
	javac -cp build:fregec*.jar -d build Main.java
CreaterForJava: build/$(PACKAGE)/CreaterForJava.class

build/$(PACKAGE)/%.class: $(PACKAGE)/%.fr
	[ -d build ] || mkdir build
	java -Xss1m -jar fregec*.jar -O -inline -d build -make $<

clean:
	rm -fr build
