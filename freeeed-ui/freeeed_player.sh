#!/bin/sh
java -Xms512m -Dlog4j.configuration=file:"config/log4j.properties" \
-cp target/freeeed-ui-1.0-SNAPSHOT-jar-with-dependencies.jar:drivers/truezip-driver-zip-7.7.4.jar \
org.freeeed.ui.FreeEedUI $1
