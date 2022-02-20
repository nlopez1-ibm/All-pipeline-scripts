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




## Below all works as separate scripts. 
## BUILD   
echo "" ; echo "" ; echo "***  BUILD.GROOVY (orig)   IMPACTBUILD "
zAppBuild="/u/nlopez/dbb-zappbuild/build.groovy" 

## USER CUASING ISSUE WITH WORKDIR
##groovyz  $zAppBuild -w $workSpace -a $app -o $workDir -h DAT.CLI --userBuild  poc-app/asm/datwto.asm 
groovyz  $zAppBuild -w $workSpace -a $app -o $workDir -h DAT.CLI  --impactBuild

if [ "$?" -ne "0" ]; then
  echo "DBB Build Error. Check the log for details. Exit 12"
  exit 12
fi



## PACKAGE
echo ""; echo "" ; echo "***   PACKAGING"
artifacts=$(ls -d $workDir/build*)
groovyz $HOME/All-pipeline-scripts/utilities/package.groovy\
  -workDir      $workDir\
  -workSpace    $artifacts\
  -application  $app\
  -version      $BUILDID\
  -s gitSourceUrl-TBD\
  -g $repo\
  -x gitSourceBranch-TBD\
  -y gitBuildBranch-TBD\
  -b 12345678\
  -n P092259\
  -u arty.none.com-TBD
  
if [ "$?" -ne "0" ]; then
  echo "DBB Packaging Error. Check the log for details. Exit 12"
  exit 12
fi




## PUBLISH with Curl  
echo "" ; echo "" ; echo "***   PUBLISHING"
curl --silent\
  --insecure -H X-JFrog-Art-Api:AKCp8ihVjqvmPbiXp6q8eEgYwYM8Xv6mqJFLHstWtb7yiKRk4GxRTdbuj2nmkHCJUQisju6WQ\
  -T  $workDir/$app.tar\
  "https:/eu.artifactory.swg-devops.com/artifactory/sys-dat-team-generic-local/$workSpace/$app/$BUILDID.tar"
  

echo ""
echo "***"



  



exit 0
  
## --- reference using groovy to curl 
## PUBLISH 
## echo "" ; echo "" ; echo "***   PUBLISHING"
## groovyz $HOME/All-pipeline-scripts/utilities/publish-v2.groovy\
##    -k AKCp8ihVjqvmPbiXp6q8eEgYwYM8Xv6mqJFLHstWtb7yiKRk4GxRTdbuj2nmkHCJUQisju6WQ\
##    -r sys-dat-team-generic-local\
##    -f $workDir/$app.tar\
##    -p DAT-CLI/$app-$BUILDID/$app.tar\
##    -u https:/eu.artifactory.swg-devops.com/artifactory
##  
## if [ "$?" -ne "0" ]; then
##  echo "DBB Publishing Error. Check the log for details. Exit 12"
##  exit 12
## fi

 