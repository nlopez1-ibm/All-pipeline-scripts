#!/bin/sh
##  Demo download a package from Art'y
##  Note my Arty_ID_Tok is in .profile
##  On zDT can use DNS to Art'y Server using its IP=169.50.87.2  to eu.artifactory.swg-devops.com
##  My version of Art'y does not support latest Version download - using one copy for demo
##
##  $1 the tar file - Fully qualified path as created by package script
##  $2 a work dir for the tar 

. /u/nlopez/.profile

tar=$1
workDir=$2
art_Repo=https:/169.50.87.2/artifactory/sys-dat-team-generic-local


echo "*** Artifactory Download with CURL***"
echo "                           Downloading Package:$tar" 
echo "                                  Into workDir:$workDir "

# reset the owner set by zDT Daemon STC
chown -R nlopez $workDir

if [ -d $workDir ]; then
    cd $workDir
    rm -rf *
else 
    mkdir -p $workDir
    cd $workDir
    
fi 
  
curl --insecure -H X-JFrog-Art-Api:$Arty_ID_Tok $art_Repo/zOS_RunBook/mypackage.tar -o mypackage.tar> Get_curl.log 2>&1  
rc=$?
chtag -b mypackage.tar
cat Get_curl.log  
  
if [ "$rc" -ne "0" ]; then
 echo "Artifactory Download Error. Check the log for details. RC=$rc"
 exit $rc
fi


# verbose mode for diag -  Note: verbose gens stderr and fails most pipelines
# curl --insecure -verbose -H X-JFrog-Art-Api:$Arty_ID_Tok -T $t  $r/AzDBB/zDT/mypackage_$v.tar > curl.log 2>&1