#!/bin/sh

# Absolute path of this script, e.g. /opt/corda/node/foo.sh
ABS_PATH=$(readlink -f "$0" 2> /dev/null)
if [ $? != 0 ]; then
	# Unfortunate MacOs issue with readlink functionality
	TARGET_FILE=$0

	cd `dirname $TARGET_FILE`
	TARGET_FILE=`basename $TARGET_FILE`

	# Iterate down a (possible) chain of symlinks
	while [ -L "$TARGET_FILE" ]
	do
		TARGET_FILE=`readlink $TARGET_FILE`
		cd `dirname $TARGET_FILE`
		TARGET_FILE=`basename $TARGET_FILE`
	done

	# Compute the canonicalized name by finding the physical path 
	# for the directory we're in and appending the target file.
	PHYS_DIR=`pwd -P`
	ABS_PATH=$PHYS_DIR/$TARGET_FILE
fi

# Absolute path of the directory this script is in, thus /opt/corda/node/
DIR=$(dirname "$ABS_PATH")

LIBS_DIR=$DIR/build/libs
JAR=webserver-0.1.jar

if [ -f "$LIBS_DIR/$JAR" ]; then
	ARGS_A="--server.port=10009 --config.rpc.host=localhost --config.rpc.port=10008 --config.rpc.username=user1 --config.rpc.password=password --spring.config.additional-location=${DIR}/resources/application.properties"
	ARGS_B="--server.port=10012 --config.rpc.host=localhost --config.rpc.port=10011 --config.rpc.username=user1 --config.rpc.password=password --spring.config.additional-location=${DIR}/resources/application.properties"
	ARGS_C="--server.port=10015 --config.rpc.host=localhost --config.rpc.port=10014 --config.rpc.username=user1 --config.rpc.password=password --spring.config.additional-location=${DIR}/resources/application.properties"
	java -jar $DIR/build/libs/$JAR $ARGS_A &
	PID_A=$!
	java -jar $DIR/build/libs/$JAR $ARGS_B &
	PID_B=$!
	java -jar $DIR/build/libs/$JAR $ARGS_C &
	PID_C=$!
	read -p "Web servers are starting up! Press enter/return to terminate them..."

	kill $PID_A
	kill $PID_B
	kill $PID_C
else
	echo "Could not find the webserver jar file named $JAR in $LIBS_DIR, listing of folder:"
	ls $LIBS_DIR
fi
