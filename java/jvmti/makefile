JAVADIR = jvm-tool-java
CPPDIR  = jvm-tool-cpp

RM	    = rm
CURDIR  = `pwd`
JSRC  	= `ls $(JAVADIR)/src/*/*/*.java`
JCLASS	= $(JAVADIR)/classes
OUTPUT	= $(CURDIR)/bin
JAROBJ  = $(OUTPUT)/jvm-ti.jar

all:_java _cpp

.PHONY:_cpp
_cpp:
	cd $(CPPDIR) && make

.PHONY:_java
_java:
	mkdir -p $(JCLASS) $(OUTPUT)
	javac -d $(JCLASS) $(JSRC)
	jar cvf  $(JAROBJ) -C $(JCLASS) .
	
.PHONY:clean
clean:
	$(RM) -rf $(OUTPUT)
	$(RM) -rf $(JCLASS)
	cd $(CPPDIR) && make clean
