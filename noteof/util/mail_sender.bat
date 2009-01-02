@echo off

echo ========================== Mail Sender==============================

set NOTEOF_HOME=C:\Projekte\workspace\noteof
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\lib\noteof.jar
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\..\happtickTest\lib\happtickTest.jar
set CLASSPATH=%CLASSPATH%;%NOTEOF_HOME%\..\happtick\lib\happtick.jar
set LIB_PATH=C:\Projekte\workspace\notioc\lib
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\notioc.jar
set CLASSPATH=%CLASSPATH%;%LIB_PATH%\jdom.jar


echo %CLASSPATH%
java de.happtick.test.MailSender --ip=192.168.0.2

pause

echo ========================== Mail Sender==============================
@echo on

