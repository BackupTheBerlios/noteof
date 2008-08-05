cd ..\bin
jar cvf ..\lib\noteof.jar de

cd ..\..\notioc\bin
jar cvf ..\lib\notioc.jar de
copy ..\lib\notioc.jar ..\..\noteof\lib

pause