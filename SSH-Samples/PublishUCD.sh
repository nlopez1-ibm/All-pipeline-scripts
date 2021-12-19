#!/bin/sh
## IBM Sample - GitLab/Azure/Jenkins DBB Demo- v1 (njl) 

. $HOME/.profile
ucd_version=$1
ucd_Component_Name=$2
MyWorkDir=$3
artifacts=$(ls -d $MyWorkDir/build*)

echo "**************************************************************"
echo "**     Started:  UCD Publish on HOST/USER: $(uname -Ia) $USER"
echo "**                                   Version:" $ucd_version
echo "**                                 Component:" $ucd_Component_Name 
echo "**                                   workDir:" $MyWorkDir   
echo "**                         DBB Artifact Path:" $artifacts
buzTool=$HOME/ucd/Tass-agent/bin/buztool.sh
groovyz $HOME/dbb-ucd-packaging.groovy -b $buzTool -w $artifacts -c $ucd_Component_Name -v $ucd_version 
