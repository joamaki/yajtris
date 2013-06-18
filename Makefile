# Makefile for YaJTris
SUBDIRS := documents source
TOPDIR  := $(PWD)
export TOPDIR

all:
	for d in $(SUBDIRS); do (cd $$d; $(MAKE) $@ ); done
	@echo
	@echo
	@echo "YaJTris compilation finished. You can find documents in the documents directory (*.pdf) and the game in the source directory."
	@echo "To start the game change to source/ and run the yajtris.sh script."

clean:
	for d in $(SUBDIRS); do (cd $$d; $(MAKE) $@ ); done

.PHONY: all clean




