# GNU Makefile
JAR=/usr/local/jdk1.8.0_131/bin/jar
JAVA=/usr/local/jdk1.8.0_131/bin/java
JAVAC=/usr/local/jdk1.8.0_131/bin/javac

JFLAGS = -g
# SOURCES = src/ADM/*.java src/Cozinha/*.java src/Mercado/*.java src/Mesa/*.java
SOURCES = src/Mercado/*.java

default: classes

classes:
	$(JAVAC) $(JFLAGS) -d out $(SOURCES)

mesa:
# 	$(JAVA) -cp out Mesa.Mesa
	$(JAVA) -cp out Mercado.Mesa

adm:
# 	$(JAVA) -cp out ADM.ADM
	$(JAVA) -cp out Mercado.ADM

cozinha:
# 	$(JAVA) -cp out Cozinha.Chef
	$(JAVA) -cp out Mercado.Chef

mercado:
# 	$(JAVA) -cp out Mercado.MercadoServidorPublisher
	$(JAVA) -cp out Mercado.MercadoServidorPublisher

clean:
	rm -f out/ADM/*.class
	rm -f out/Cozinha/*.class
	rm -f out/Mercado/*.class
	rm -f out/Mesa/*.class
