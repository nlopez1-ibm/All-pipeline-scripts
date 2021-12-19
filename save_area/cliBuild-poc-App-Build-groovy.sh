#!/bin/sh
##  Demo Build, package and pub to Art'y
## Cert - dont break me please 

clear 

BUILDID=$(date "+%Y%m%d.%H%M%S")
workDir=$HOME/tmp/DAT-CLI-BUILDS/RUN-$BUILDID
mkdir -p $workDir
cd $workDir
repo="git@github.ibm.com:Nelson-Lopez1/poc-workspace.git"
app="poc-app"
workSpace=$(basename $repo) ;workSpace=${workSpace%.*}


 
## CLONE   
git clone  $repo
git -C $workSpace log  --oneline -n 1  --simplify-by-decoration 	


## BUILD  with V2   embedded pack/pub  
echo "" ; echo "" ; echo "***  Build v2    userBuild mode to skip webApp"

zAppBuild="/u/nlopez/dbb-zappbuild/build-v2.groovy"
groovyz  $zAppBuild -w $workSpace -a $app -o $workDir -h DAT.CLI  --userBuild poc-app/asm/datwto.asm    
if [ "$?" -ne "0" ]; then
  echo "DBB Build Error. Check the log for details. Exit 12"
  exit 12
fi
exit 0

 