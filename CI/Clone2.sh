#!/bin/sh
## Demo script git clone v2.0 (nlopez)
## Added support for pull vs full clone for performance
## TODO: The pull request can switch to diff brnach then previouly cloned - yet!    
. /u/nlopez/.profile
#set -x
workDir=$1 
workSpace=$2
repoUrl=$3
repoRef=$4

#  cleanup repos in the workdir older than 90 days       
##   skulker -r /u/nlopez/tmp/ 90
      
# Strip any 'refs/heads/...'    
if [[ $repoRef = *"refs/heads/"* ]]; then 
    repoRef=${repoRef##*"refs/heads/"}    
fi   

# for common repos switch to the main app repo's workDir clone before the common
if [ -d $workDir/$workSpace ]; then
   cd $workDir/$workSpace
   gitCmd="git pull "
  
else 
    mkdir -p $workDir
    cd $workDir
    gitCmd="git clone -b $repoRef $repoUrl "    
fi 
 
echo "**************************************************************"
echo "**     Started:  Rocket-Git Clone on HOST/USER: $(uname -Ia)/$USER"
echo "**                               Repo Url:" $repoUrl  
echo "**                               Repo Ref:" $repoRef
echo "**                              workSpace:" $workSpace 
echo "**                          WorkDir (pwd):" $PWD
echo "**                                Git Cmd:" $gitCmd 
echo "**                            Git Version: $(git --version)"
echo "**"
pwd
git config --global  advice.detachedHead false
$gitCmd    2>&1
if [ -d $workSpace ]; then    cd $workSpace ; fi
echo " "
echo "GitLog: " 
git log --graph --oneline --decorate -n 3
