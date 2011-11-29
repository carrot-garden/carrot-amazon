#!/bin/bash

S_USER="ubuntu"

#

K_USER="karaf"
K_GROUP="karaf"
K_HOME="/var/karaf"

#

JAVA_JDK="jdk1.7.0_01"
JAVA_URL="http://download.oracle.com/otn-pub/java/jdk/7u1-b08/jdk-7u1-linux-i586.tar.gz"
JAVA_ZIP=$(basename $JAVA_URL)

JAVA_NAME="java"
JAVA_LINK="/usr/bin/$JAVA_NAME"
JAVA_DIR="/opt/$JAVA_NAME"
JAVA_EXE="$JAVA_DIR/$JAVA_JDK/bin/java"

#

APT_GAP=$(expr $(date +%s) - $(stat --format %X /var/lib/apt/lists) )
APT_DUE=$(expr 24 \* 3600)

echo "### APT_GAP=$APT_GAP"
echo "### APT_DUE=$APT_DUE"

function run(){

	local command="$@"

	echo "############"
	echo "### $command"
	
	$command
	
	local result=$?

	echo "### result=$result"
	
	if [[ $result != 0 ]] ; then
		echo "### error"
		exit 1
	fi

	echo "############"
	
}

###

echo "### home"

run ls -las /home/$S_USER

echo "### apt update"

if [[ $APT_GAP > $APT_DUE ]] ; then
	 apt-get --assume-yes update
	 apt-get --assume-yes upgrade
fi

echo "### apt install"

run apt-get --assume-yes install mc tar wget zip unzip

echo "### java install"

run mkdir --parents $JAVA_DIR

cd $JAVA_DIR

run wget --timestamping --progress=bar:force $JAVA_URL

run tar --extract --gzip --keep-newer-files --totals --file $JAVA_ZIP 2> /tmp/tar-err.log


run update-alternatives --remove-all $JAVA_NAME

run update-alternatives --install $JAVA_LINK $JAVA_NAME $JAVA_EXE 10

run java -version 2>&1

echo "### karaf"

addgroup --system $K_GROUP
adduser --system --ingroup $K_GROUP --home $K_HOME $K_USER
adduser $S_USER $K_GROUP

chown --changes --recursive $S_USER:$K_GROUP $K_HOME
chmod --changes --recursive o-rwx,g+rw,ugo-s $K_HOME
find $K_HOME -type d -exec chmod --changes g+s {} \;

