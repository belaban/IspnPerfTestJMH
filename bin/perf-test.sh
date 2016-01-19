#!/bin/bash
# Author: Bela Ban

curr_dir=`dirname $0`

if [ -f $HOME/log4j.properties ]; then
    LOG="-Dlog4j.configuration=file:$HOME/log4j.properties"
fi;

if [ -f $HOME/log4j2.xml ]; then
    LOG="$LOG -Dlog4j.configurationFile=$HOME/log4j2.xml"
fi;

if [ -f $HOME/logging.properties ]; then
    LOG="$LOG -Djava.util.logging.config.file=$HOME/logging.properties"
fi;

FLAGS="-Djava.net.preferIPv4Stack=true"

mvn -f ${curr_dir}/../pom.xml  ${FLAGS} ${LOG} exec:java -Dexec.mainClass=$1
