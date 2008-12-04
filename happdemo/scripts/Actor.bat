@echo off
echo ========================== Actor ==============================

set NOTEOF_HOME=C:\Projekte\workspace\noteof
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\lib\noteof.jar
set LIB_PATH=C:\Projekte\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jdom.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\happtick\lib\happtick.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\happdemo\bin


echo %CLASSPATH%
java de.happdemo.Actor --serverIp=localhost --serverPort=3000 %*

pause

echo ========================== Actor ==============================
@echo on

