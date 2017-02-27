app/src/main/res/layout/board.xml: app/src/main/haml/board.haml
	haml $< >$@

install: build
	./install.sh 

build:
	gradle assembleRelease

build-frege:
	gradle -Pfrege assembleRelease

clean:
	gradle clean
