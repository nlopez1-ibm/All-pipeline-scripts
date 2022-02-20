#!/bin/sh
##  Demo publish a tar file  to Art'y
##  Note my Arty_ID_Tok is def in .profile
##  on zDT can use DNS to arty Server using its IP=169.50.87.2  #eu.artifactory.swg-devops.com
##
##  $1 the tar file - Fully qualified path as created by package script
##  $2 project name - dbb workspace used as art_repo folder name       
. /u/nlopez/.profile

tar=$1
art_Repo=https:/169.50.87.2/artifactory/sys-dat-team-generic-local


echo "*** Publishing with CURL***"
echo "                  Package:$tar" 

rm -f Put_curl.log 
curl --insecure -H X-JFrog-Art-Api:$Arty_ID_Tok -T $tar  $art_Repo/zOS_RunBook/mypackage.tar > Put_curl.log 2>&1
rc=$?
cat Put_curl.log  
  
if [ "$rc" -ne "0" ]; then
 echo "DBB Publishing Error. Check the log for details. RC=$rc"
 exit $rc
fi


