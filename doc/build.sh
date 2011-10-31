#!/bin/bash

# Harsh on errors
set -e

TEMPLATE=conf/template.html
TARGET=deploy
ACTIONS=build
CURRENT_ACTION=

require() {
	REQUIRES="$REQUIRES $1"
}

check() {
	for c in $REQUIRES; do
		if [ ! -e "`which $c`" ]; then
			echo "ERROR: $c must be installed" 1>&2
			MISSING=true
		fi
	done
	
	if [ $MISSING ]; then
		exit 100
	fi
}

log() {
	echo -e "[\033[1m${CURRENT_ACTION}\033[0m] " $* 1>&2
}

error() {
	echo -e "[\033[31m${CURRENT_ACTION}\033[0m] " $* 1>&2
}


# require ack
require rst2html
check

cd `dirname $0`

[ "$1" == "" ] || ACTIONS="$*"

ERROR=false
for action in $ACTIONS; do
	CURRENT_ACTION=$action
	case $action in
		build)
			log 'Building all source files...'
			[ -e $TARGET ] || mkdir $TARGET
			for f in src/*.rst; do
				fname=`echo $f | sed 's!^src/!!g' | sed 's/.txt$//g'`.html
				
				log "    $f"

				rst2html --halt=2 --report=2 --template=$TEMPLATE $f $TARGET/$fname
				
				if [ $? -ne 0 ]; then
					ERROR=true
				fi
				
				# ack '^<title>([^<]*)</title>$' --output '<title>ReportGrid API: $1</title>' --passthru $TARGET/$fname > /tmp/$fname
				# mv /tmp/$fname $TARGET/$fname
				# 
				# ack '^<title>ReportGrid API: </title>$' --output '<title>ReportGrid API</title>' --passthru $TARGET/$fname > /tmp/$fname
				# mv /tmp/$fname $TARGET/$fname
			done
			
			log 'Copying static files...'
			cp -r static/* $TARGET
			log 'Complete'
		;;
		
		deploy)
		        if [ "$ERROR" = "true" ]; then
			    log "Cannot deploy due to preceding errors"
			else
			    if [ ! -e $TARGET ]; then
				ERROR=true
				log 'Target directory does not exist!'
			    else
				log 'Syncing target directory to server...'
				
				cd `dirname $0`

				s3cmd -v -F --acl-public --add-header='Cache-Control:max-age=0' sync -r deploy/ s3://api.reportgrid.com/doc/
			    fi
			fi
		;;
		
		clean)
			log "Removing target directory..."
			rm -rf $TARGET
			log 'Complete'
		;;
		
		*)
			error 'Unrecognized action!'
			exit -1
		;;
	esac
	
	if [ "$ERROR" == true ]; then
		error 'One or more tasks exited with an error status...'
		exit -1
	fi
done

echo -e '[\033[32msuccess\033[0m] All done!' 1>&2
