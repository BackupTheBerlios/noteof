@echo off

rem copy ..\..\notioc\lib\notioc.jar ..\lib

set XNOTEOF_HOME=C:\Projekte\workspace\noteof
set CLASSPATH=%CLASSPATH%;%XNOTEOF_HOME%\lib\noteof.jar
set LIB_PATH=C:\Projekte\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jdom.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\happtick\lib\happtick.jar

@echo on
java de.notEOF.core.server.Server --port=3000 --homeVar=XNOTEOF_HOME

pause