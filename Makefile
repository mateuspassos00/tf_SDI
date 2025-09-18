# GNU Makefile
JAR=/usr/local/jdk1.8.0_131/bin/jar
JAVA=/usr/local/jdk1.8.0_131/bin/java
JAVAC=/usr/local/jdk1.8.0_131/bin/javac

JFLAGS = -g
SOURCES = src/ADM/*.java src/Cozinha/*.java src/Mercado/*.java src/Mesa/*.java

default: classes

classes:
	$(JAVAC) $(JFLAGS) -d out $(SOURCES)

clean:
	rm -f out/ADM/*.class
	rm -f out/Cozinha/*.class
	rm -f out/Mercado/*.class
	rm -f out/Mesa/*.class
