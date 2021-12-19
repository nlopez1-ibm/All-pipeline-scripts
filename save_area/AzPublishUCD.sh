#!/bin/sh
## GitLab/Azure/Jenkins DBB Demo- UCD Buztool v1.3 (njl) 

. $HOME/.profile
ucd_version=$1
ucd_Component_Name=$2
MyWorkDir=$3
artifacts=$(ls -d $MyWorkDir/build*)
artyProp=$3/$4/$5

 

echo "***! UCD 7 - setting libpath for this ver of UCD ==> LIBPATH=/usr/lib:$LIBPATH"  
export LIBPATH=/usr/lib:$LIBPATH

echo "**************************************************************"
echo "**     Started:  UCD Publish on HOST/USER: $(uname -Ia) $USER"
echo "**                                   Version:" $ucd_version
echo "**                                 Component:" $ucd_Component_Name 
echo "**                                   workDir:" $MyWorkDir   
echo "**                         DBB Artifact Path:" $artifacts
echo "**                     Artifactory Prop File:" $artyProp 
## Change the home path for buztool and deploy.groovy to match your installation paths  
# ver 6.1  Deprecated 
##  buzTool=$HOME/ucd/Tass-agent/bin/buztool.sh

# ver 7.1.1
buzTool=$HOME/ucd7/bin/buztool.sh

# old groovyz $HOME//Azure-pipeline-scripts/deploy.groovy -b $buzTool -w $artifacts -c $ucd_Component_Name -uver $ucd_version
groovyz $HOME//Azure-pipeline-scripts/dbb-ucd-packaging.groovy -b $buzTool -w $artifacts -c $ucd_Component_Name -v $ucd_version -prop $artyProp
