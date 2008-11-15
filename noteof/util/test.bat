rem @echo off

set NOTEOF_HOME=C:\Projekte\workspace\noteof
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\lib\noteof.jar
set LIB_PATH=C:\Projekte\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jdom.jar

echo %CLASSPATH%

@echo on
java de.notEOF.test.Test


C:\Projekte\workspace\noteof\util\happtick_test.bat

pause