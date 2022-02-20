#!/bin/sh
##  untar a package  
. /u/nlopez/.profile

workDir=$1
tar=$2
echo "*** unPackage Tar fil***"
echo "                     Package:$tar" 
echo "                Into workDir:$workDir "

cd $workDir
tar -xvf $tar
rc=$?
 
if [ "$rc" -ne "0" ]; then
 echo "unTar Error. Check the log for details. RC=$rc"
 exit $rc
fi