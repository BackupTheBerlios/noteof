echo ============================== Server ==============================

export NOTEOF_HOME=/home/dgo01/workspace/noteof
export CLASSPATH=$CLASSPATH:$NOTEOF_HOME/lib/noteof.jar
export LIB_PATH=/home/dgo01/workspace/notioc/lib
export CLASSPATH=$CLASSPATH:$LIB_PATH/notioc.jar
export CLASSPATH=$CLASSPATH:$LIB_PATH/jdom.jar
export CLASSPATH=$CLASSPATH:/home/dgo01/workspace/happtick/lib/happtick.jar

export JAVA_HOME=/usr/java/jdk1.6.0_11/jre

echo $CLASSPATH
echo $LIB_PATH
$JAVA_HOME/bin/java -version


$JAVA_HOME/bin/java de.notEOF.core.server.Server --port=3000 --homeVar=NOTEOF_HOME

echo ============================== Server ==============================
