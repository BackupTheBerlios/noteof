rem @echo off

set NOTEOF_HOME=C:\Projekte\workspace\noteof
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\lib\noteof.jar
set LIB_PATH=C:\Projekte\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jdom.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\happtick\lib\happtick.jar


echo %CLASSPATH%
java de.happtick.test.TestHapptick

pause
@echo on

