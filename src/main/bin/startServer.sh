#!/usr/bin/env bash


FINDNAME=$0
while [ -h $FINDNAME ] ; do FINDNAME=`ls -ld $FINDNAME | awk '{print $NF}'` ; done
SERVER_HOME=`echo $FINDNAME | sed -e 's@/[^/]*$@@'`
unset FINDNAME

if [ "$SERVER_HOME" = '.' ]; then
   SERVER_HOME=$(echo `pwd` | sed 's/\/bin//')
else
   SERVER_HOME=$(echo $SERVER_HOME | sed 's/\/bin//')
fi

if [ ! -d $SERVER_HOME/pids ]; then
    mkdir $SERVER_HOME/pids
fi

HEAP_MEMORY=2048m
PERM_MEMORY=64m
SERVER_NAME=lss-mqueue
JMX_PORT=6691
PIDFILE=$SERVER_HOME/pids/$SERVER_NAME.pid

case $1 in
start)
    echo  "Starting $SERVER_NAME ... "

    JAVA_OPTS="-server -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

    shift
    ARGS=($*)
    for ((i=0; i<${#ARGS[@]}; i++)); do
        case "${ARGS[$i]}" in
        -D*)    JAVA_OPTS="${JAVA_OPTS} ${ARGS[$i]}" ;;
        -Heap*) HEAP_MEMORY="${ARGS[$i+1]}" ;;
        -Perm*) PERM_MEMORY="${ARGS[$i+1]}" ;;
        esac
    done
    JAVA_OPTS="${JAVA_OPTS} -Xms${HEAP_MEMORY} -Xmx${HEAP_MEMORY} -XX:PermSize=${PERM_MEMORY} -XX:MaxPermSize=${PERM_MEMORY} -Dcom.sun.management.jmxremote.port=${JMX_PORT} -Duser.dir=${SERVER_HOME} -Dapp.name=$SERVER_NAME"
    echo "start jvm args ${JAVA_OPTS}"
    nohup java $JAVA_OPTS -jar ${SERVER_HOME}/$SERVER_NAME.jar >/dev/null &
    echo $! > $PIDFILE
    echo STARTED
    ;;

stop)
    echo "Stopping $SERVER_NAME ... "
    if [ ! -f $PIDFILE ]
    then
        echo "error: count not find file $PIDFILE"
        exit 1
    else
        kill -15 $(cat $PIDFILE)
        rm $PIDFILE
        echo STOPPED
    fi
    ;;

restart)
    ./startServer.sh stop
    sleep 1
    ./startServer.sh start
    ;;


*)
    echo "PLEASE INPUT COMMAND ... "
    ;;

esac

exit 0

