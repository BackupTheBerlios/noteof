@echo off
echo ============================== Server ==============================

set NOTEOF_HOME=C:\Projekte\workspace\noteof
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\lib\noteof.jar
set LIB_PATH=C:\Projekte\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jdom.jar
rem set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\happtick\lib\happtick.jar

@echo on
java de.notEOF.core.server.Server --port=3000 --homeVar=NOTEOF_HOME

echo ============================== Server ==============================
pause