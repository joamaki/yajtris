# Makefile for YaJTris/source
ifeq ($(strip $(TOPDIR)),)
	# Assume TOPDIR is one level down if we weren't called recursively
	TOPDIR	 	    = $(PWD)/..
endif
SRC_DIR 		= $(TOPDIR)/source
CLASS_DIR       = $(TOPDIR)/source/classes

# Sources with main() in them
MAIN_FILES = Game.java Test1.java Test2.java

#JDEBUGFLAGS	= -g -deprecation
JDEBUGFLAGS	= -O 
JCC		= javac
JDOC 	= javadoc

all: clean classes jar javadoc

# create target class dir if not present.
$(CLASS_DIR):
	mkdir -p $(CLASS_DIR)

.SUFFIXES:
.SUFFIXES: .java .class

vpath %.class $(CLASS_DIR)

.java.class:
	$(JCC) -nowarn -d $(CLASS_DIR) $(JDEBUGFLAGS) $*.java

# find out target files

classes: $(CLASS_DIR) $(MAIN_FILES:.java=.class)

jar:
	cd $(CLASS_DIR) && jar cvfm $(SRC_DIR)/yajtris.jar $(SRC_DIR)/manifest *.class

javadoc:
	cd $(TOPDIR)/javadoc && $(JDOC) -private -author -version $(SRC_DIR)/*.java

clean:
	cd $(SRC_DIR) && \
		rm -f *~ *.class *.jar core *.bak $(CLASS_DIR)/*.class

test:
	cd $(CLASS_DIR) && for class in Test*.class; do \
		java `echo $$class|cut -d. -f1`; done

