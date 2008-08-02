set NOTEOF_HOME=c:\Projekte\workspace\noteof

set CLASSPATH=c:\Projekte\workspace\noteof\lib\noteof.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\noteof\lib\notioc.jar

set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\notioc\lib\jaxen-1.1.1.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\notioc\lib\dom4j-1.6.1.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\notioc\lib\commons-logging-1.0.4.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\notioc\lib\commons-configuration-1.4.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\notioc\lib\commons-lang-2.3.jar
set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\notioc\lib\commons-collections-3.2.jar

rem set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\noteof\lib\xstream-1.2.jar
rem set CLASSPATH=%CLASSPATH%;c:\Projekte\workspace\noteof\lib\jdom-1.0.jar
echo %CLASSPATH%

java de.notEOF.test.Test
