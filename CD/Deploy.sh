#!/bin/sh
## Deploy DBB Package to target LPAR v1.3 (njl)
. /u/nlopez/.profile

workDir=$1 ; cd $workDir

echo "**************************************************************"
echo "**     Started:  Deployment on HOST/USER: $(uname -Ia)/$USER"
echo "**                           workDir:" $PWD

deploy=/u/nlopez/All-pipeline-scripts/utilities/Deploy.groovy
groovyz $deploy  -w $workDir 
  
if [ "$?" -ne "0" ]; then
 echo "Deployment Error. Check the log for details. RC=$?"
 exit $rc
fi
