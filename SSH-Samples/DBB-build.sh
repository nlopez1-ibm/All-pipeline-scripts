#!/bin/sh
## IBM Sample Demo- DBB Build v1.2 (njl)
. $HOME/.profile
MyWorkDir=$1 ; cd $MyWorkDir
MyWorkSpace=$2
MyApp=$3
BuildMode="$4 $5" #DBB Build modes:  --impactBuild,  --reset, --fullBuild, '--fullbuild --scanOnly'
zAppBuild=$HOME/dbb-zappbuild/build.groovy
echo "**************************************************************" 
echo "**     Started:  DBB Build on HOST/USER: $(uname -Ia)/$USER"
echo "**                        MyWorkDir:" $PWD
echo "**                      MyWorkspace:" $MyWorkSpace   
echo "**                            MyApp:" $MyApp
echo "**                   DBB Build Mode:" $BuildMode
echo "**                   zAppBuild Path:" $zAppBuild
echo "**                         DBB_HOME:" $DBB_HOME 
echo "** "
echo " ** Git Status  for MyWorkSpace:"  
git -C $MyWorkSpace status  
groovyz $zAppBuild  --workspace $MyWorkSpace --application $MyApp  -outDir . --hlq $USER  --logEncoding UTF-8  $BuildMode

if [ "$?" -ne "0" ]; then
  echo "DBB Build Error. Check the build log for details"
  exit 12
fi

exit 0