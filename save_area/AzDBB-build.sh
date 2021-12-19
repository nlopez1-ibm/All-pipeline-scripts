#!/bin/sh
## Az/DBB Demo- DBB Build v1.2 (njl) ++ zdt daemon

. $HOME/.profile
WorkDir=$1 ; cd $WorkDir
WorkSpace=$2
App=$3
BuildMode="$4 $5" #DBB Build modes:  --impactBuild,  --reset, --fullBuild, '--fullBuild --scanOnly'
#zAppBuild=$HOME/dbb-zappbuild/build.groovy
zAppBuild=$HOME/DBB-POC/dbb-zappbuild/build.groovy

echo "**************************************************************"
echo "**    ./AzDBB-build.sh on HOST/USER: $(uname -Ia)/$USER        v1.3"
echo "**                          WorkDir:" $PWD
echo "**                        Workspace:" $WorkSpace
echo "**                              App:" $App
echo "**    	            DBB Build Mode:" $BuildMode
echo "**               DBB zAppBuild Path:" $zAppBuild
echo "**                         DBB_HOME:" $DBB_HOME
echo "** "

echo "\n** Git Status for $WorkDir/$WorkSpace:"
git -C $WorkSpace status


echo   "groovyz -DBB_DAEMON_PORT 8080 $zAppBuild  --workspace $WorkDir/$WorkSpace --application $App  -outDir . --hlq $USER.SSH  --logEncoding UTF-8  $BuildMode"
groovyz    -DBB_DAEMON_PORT 8080     $zAppBuild  --workspace $WorkDir/$WorkSpace --application $App  -outDir . --hlq $USER.SSH  --logEncoding UTF-8  $BuildMode

if [ "$?" -ne "0" ]; then
  echo "DBB Build Error. Check the build log for details"
  exit 12
fi


## Except for the reset or scanOnly modes, check for "nothing to build" condition and throw an error to stop pipeline
if [[ $BuildMode = '--reset'  || $BuildMode = '--scanOnly' ]];  then
	buildlistsize=$(wc -c < $(find . -name buildList.txt)) 
	if [ $buildlistsize = 0 ]; then 
    	echo "*** Build Error:  No source changes detected. Stopping pipeline.  RC=12"
    	exit 12    
	fi
else
	echo "*** DBB reset or scanOnly completed"
fi
exit 0 


## groovyz -DBB_DAEMON_PORT 8080 /u/nlopez/DBB-POC/dbb-zappbuild/build.groovy  -w poc-workspace -a poc-app  -outDir . --hlq NLOPEZ.SSH  --logEncoding UTF-8  --impactBuild
## groovyz  /u/nlopez/DBB-POC/dbb-zappbuild/build.groovy  -w poc-workspace -a poc-app  -outDir . --hlq NLOPEZ.SSH  --logEncoding UTF-8  --impactBuild