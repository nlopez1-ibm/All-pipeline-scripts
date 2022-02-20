#!/bin/sh
## Demo script git clone v1.3 (nlopez)
. /u/nlopez/.profile

workDir=$1 
workSpace=$2
myRepo=$3
ref=$4
      
# Strip any 'refs/heads/...'    
if [[ $ref = *"refs/heads/"* ]]; then 
    ref=${ref##*"refs/heads/"}    
fi   

# for common repos switch to the main app repo's workSpace previously created by the first clone step
if [ -d $workDir/$workSpace ]; then
   cd $workDir/$workSpace
else 
    mkdir -p $workDir
    cd $workDir
fi 
 
echo "**************************************************************"
echo "**     Started:  Rocket-Git Clone on HOST/USER: $(uname -Ia)/$USER"
echo "**                                   Repo:" $myRepo
echo "**                                    Ref:" $ref
echo "**                          WorkDir (pwd):" $PWD
echo "**                              WorkSpace:" $workSpace
echo "**                            Git Version: $(git --version)"
echo "**"

git config --global  advice.detachedHead false
git clone -b $ref $myRepo  2>&1

