LAYOUT_DIR = app/src/main/res/layout
HAML_DIR = app/src/main/haml

haml: $(LAYOUT_DIR)/board.xml $(LAYOUT_DIR)/activity_help.xml

$(LAYOUT_DIR)/board.xml: $(HAML_DIR)/board.haml
	haml $< >$@

$(LAYOUT_DIR)/activity_help.xml: $(HAML_DIR)/activity_help.haml
	haml $< | sed s/__/./g >$@

install: build
	./install.sh 

build:
	gradle assembleRelease

build-frege:
	gradle -Pfrege assembleRelease

clean:
	gradle clean
