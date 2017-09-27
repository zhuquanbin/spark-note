CC  = gcc
CXX = g++
MD  = mkdir
RM	= rm -rf

INCLUDE = -I/home/prod/softwares/jdk1.8.0_60/include/ -I/home/prod/softwares/jdk1.8.0_60/include/linux/
CFLAGS	= -g -Wall -fPIC $(INCLUDE)

SRC  	= src
BUILD	= build
OBJDIR	= $(BUILD)/obj
BINDIR  = ../bin

#source 	= $(wildcard *.cpp)  
#objs	= $(source:%.cpp=$(OBJDIR)/%.o)

# JVM Tool Interface dynamic library
JVMTIAGENT 	= $(BINDIR)/libagent.so
# Jar Package class encrypt dynamic library
ENCRYPTLIB	= $(BINDIR)/libencrypt.so

all: _pre $(JVMTIAGENT) $(ENCRYPTLIB)

$(JVMTIAGENT): $(OBJDIR)/jvmtimain.o $(OBJDIR)/jvmtiagent.o
	$(CXX) -shared  -o  $@ $^

$(ENCRYPTLIB): $(OBJDIR)/jarencrypt.o
	$(CXX) -shared  -o  $@ $^
	
$(OBJDIR)/%.o:$(SRC)/%.cpp
	$(CXX) $(CFLAGS) -c $< -o $@  


.PHONY:_pre
_pre:
	$(MD) -p $(OBJDIR) $(BINDIR)

.PHONY:clean
clean:
	$(RM) $(BUILD)/*
