#!/bin/sh
##  Demo publish a tar file  to Art'y  using Rocket's Curl tool (NLopez)
##  Note my Arty_ID_Tok is def in .profile
##  on zDT can use DNS to arty Server using its IP=169.50.87.2  #eu.artifactory.swg-devops.com
##  $1 is the local tar file  (Fully qualified path)
##  $2 ver#    
##   TEST RESULTS: PASSED 
##   TODO add ip to /etc/host... 
clear 
. /u/nlopez/.profile

t=$1
v=$2
r=https:/169.50.87.2/artifactory/sys-dat-team-generic-local


## PUBLISH with Curl  Note: verbose gens stderr and fails a pipeline so redirecting it
## Get the get from Jfron Home page
echo "" ; echo "" ; echo "***   PUBLISHING $1"
curl --insecure -H X-JFrog-Art-Api:$Arty_ID_Tok -T $t  $r/AzDBB/zDT/mytar_$v.tar > curl.log 2>&1
cat curl.log  
  
 
if [ "$?" -ne "0" ]; then
 echo "DBB Publishing Error. Check the log for details. Exit 12"
 exit 12
fi




## Ref for testing 
#curl -verbose --insecure -H X-JFrog-Art-Api:$Arty_ID_Tok -T  z https:/169.50.87.2/artifactory/sys-dat-team-generic-local/AzDBB/zDTP/mytar_$v.tar
# raw from jfrog set-me-up
#curl -H "X-JFrog-Art-Api:<API_KEY>" -T <PATH_TO_FILE> "https://eu.artifactory.swg-devops.com/artifactory/sys-dat-team-generic-local/<TARGET_FILE_PATH>"
#t=/u/nlopez/CI-PIPELINES-WorkDir/AzDBB_1926/poc-workspace/MyTar_1926.tar  