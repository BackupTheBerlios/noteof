echo ============================== Scheduler ==============================

export NOTEOF_HOME=/home/dgo01/workspace/noteof
export CLASSPATH=$CLASSPATH;$NOTEOF_HOME/lib/noteof.jar
export LIB_PATH=/home/dgo01/workspace/notioc/lib
export CLASSPATH=$CLASSPATH;$LIB_PATH/notioc.jar
export CLASSPATH=$CLASSPATH;$LIB_PATH/jdom.jar
export CLASSPATH=$CLASSPATH;/home/dgo01/workspace/happtick/lib/happtick.jar

export JAVA_HOME=/usr/java/jdk1.6.0_11/jre

echo $CLASSPATH
echo $JAVA_HOME/bin/java -version

$JAVA_HOME/bin/java de.happtick.core.schedule.Scheduler --homeVar=NOTEOF_HOME --baseConfFile=noteof_master.xml --baseConfPath=conf --port=3000


echo ============================== Scheduler ==============================
