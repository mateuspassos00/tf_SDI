# GNU Makefile
JAR=/usr/local/jdk1.8.0_131/bin/jar
JAVA=/usr/local/jdk1.8.0_131/bin/java
JAVAC=/usr/local/jdk1.8.0_131/bin/javac

JFLAGS = -g
SOURCES = $(wildcard src/*.java)

default: classes

classes:
	$(JAVAC) $(JFLAGS) $(SOURCES)


clean:
	rm -f src/*.class 
