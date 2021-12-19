#!/bin/sh
## Az/DBB Demo- Deploy DBB Package to target LPAR v1.3 (njl)

. $HOME/.profile

WorkDir=$1 ; cd $WorkDir
tar=~/$WorkDir/$2.tar
#pdsMapping=$3

echo "**************************************************************"
echo "**     Started:  DBB Build on HOST/USER: $(uname -Ia)/$USER"
echo "**                           WorkDir:" $PWD
echo "**                       Tar Package:" $tar 
echo "** "

deploy=$HOME/Azure-pipeline-scripts/utilities/DeployUtilities.groovy
groovyz $deploy  -w $WorkDir -t $tar