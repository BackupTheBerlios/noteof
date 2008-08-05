copy ..\..\notioc\lib\notioc.jar ..\lib

set NOTEOF_HOME=C:\Dokumente und Einstellungen\Gerlinde\Eigene Dateien\workspace\noteof
set LIB_PATH=C:\Dokumente und Einstellungen\Gerlinde\Eigene Dateien\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\lib\noteof.jar

set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar


set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jaxen-1.1.1.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\commons-logging-1.0.4.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\commons-configuration-1.4.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\commons-lang-2.3.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\commons-collections-3.2.jar

java de.notEOF.core.server.Server --port=3000

pause