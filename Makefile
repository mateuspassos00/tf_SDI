# GNU Makefile
JAR=/usr/local/jdk1.8.0_131/bin/jar
JAVA=/usr/local/jdk1.8.0_131/bin/java
JAVAC=/usr/local/jdk1.8.0_131/bin/javac

JFLAGS = -g
SOURCES = src/ADM/*.java src/Cozinha/*.java src/Mercado/*.java src/Mesa/*.java src/Filial/*.java
#SOURCES = src/Mercado/*.java src/Filial/*.java
FILIAL_CP = out:lib/*

default: classes

classes:
	$(JAVAC) $(JFLAGS) -cp "lib/*" -d out $(SOURCES)

mesa:
	$(JAVA) -cp out Mesa.Mesa
# 	$(JAVA) -cp out Mercado.Mesa

adm:
	$(JAVA) -cp out ADM.ADM
# 	$(JAVA) -cp out Mercado.ADM

cozinha:
	$(JAVA) -cp out Cozinha.Chef
# 	$(JAVA) -cp out Mercado.Chef

mercado:
	$(JAVA) -cp out Mercado.MercadoServidorPublisher

filial1:
	$(JAVA) -cp $(FILIAL_CP) Filial.FilialServer 9001

filial2:
	$(JAVA) -cp $(FILIAL_CP) Filial.FilialServer 9002

filial3:
	$(JAVA) -cp $(FILIAL_CP) Filial.FilialServer 9003

filial4:
	$(JAVA) -cp $(FILIAL_CP) Filial.FilialServer 9004

filial5:
	$(JAVA) -cp $(FILIAL_CP) Filial.FilialServer 9005

clean:
	rm -f out/ADM/*.class
	rm -f out/Cozinha/*.class
	rm -f out/Mercado/*.class
	rm -f out/Mesa/*.class
	rm -f out/Filial/*.class