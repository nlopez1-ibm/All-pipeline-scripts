#!/bin/sh
## Az/DBB Demo- DBB Build v1.2 (njl) ++ zdt daemon

. /u/nlopez/.profile

WorkDir=$1
WorkSpace=$2
App=$3
BuildMode="$4 $5" #DBB Build modes:  --impactBuild,  --reset, --fullBuild, '--fullBuild --scanOnly'

cd $WorkDir
zAppBuild=$HOME/dbb-zappbuild/build.groovy
#runDBB="groovyz -DBB_DAEMON_PORT 8080 $zAppBuild  --workspace $WorkDir/$WorkSpace --application $App  -outDir $WorkDir/$WorkSpace --hlq $USER.IDz  --logEncoding UTF-8  $BuildMode"
runDBB="groovyz $zAppBuild  --workspace $WorkDir/$WorkSpace --application $App  -outDir $WorkDir/$WorkSpace --hlq $USER.IDz  --logEncoding UTF-8  $BuildMode"


echo "**************************************************************"
echo "**  ./AzDBB-build.sh v1.3 HOST/USER: $(uname -Ia)/$USER  zDT (DBB Daemon-Mode)"
echo "**                          WorkDir:" $PWD
echo "**                        Workspace:" $WorkSpace
echo "**                              App:" $App
echo "**    	           DBB Build Mode:" $BuildMode
echo "**               DBB zAppBuild Path:" $zAppBuild
echo "**                         DBB_HOME:" $DBB_HOME
echo "**               DBB Tookit Version: $(head -n 1 $DBB_HOME/bin/version.properties)"

echo "** "

echo "\n** Git Status for $WorkDir/$WorkSpace:"
git -C $WorkSpace status

echo $runDBB 
$runDBB 
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