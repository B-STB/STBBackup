#!/bin/sh
#Startup script for STBBackup software

check_process() {
PROCESS=$1
PIDS=`ps cax | grep $PROCESS | grep -o '^[ ]*[0-9]*'`
if [ -z "$PIDS" ]; then
  echo "Process not running." 1>&2
  return 1
else
  for PID in $PIDS; do
    echo $PID
  done
  return 0
fi
}
start_stb() {
rm -f nohup.out
nohup java -cp "./lib/stbcontroller.jar:./conf/stb.properties:./conf/logback.xml:./conf/*:./lib/*" org.stb.main.Main &> nohup.out
}


ts=`date +%T`

if check_process "stbcontroller"
then
echo "stb service is already running. Do you want to restart it?"
else
echo "Starting STB"
start_stb
fi
