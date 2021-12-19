#!/bin/sh
## NOTE - - this sample was to demo cloning a common repo via tags. 
# I later found out that I can reuse the original clone script has the clone command takes a tag
##  as the branch name 

## keeping it for future ref


 
## Az/DBB Demo- For multi Repo Projects (MyWorkSpace pre-created in the first clone job)
## added tagging model for better cross team support
   
. $HOME/.profile

MyRepo=$1; MyWorkDir=$2; branch=$3; MyWorkSpace=$4; MyTag=$5 
cd $MyWorkDir/$MyWorkSpace 

# TODO Strip the prefix of the branch for cloning 
# branch=${branch##*"refs/heads/"}   

CommonSrcDir=$(basename $MyRepo) ;workSpace=${workSpace%.*}
echo "**************************************************************"
echo "**     Started: Git Clone v3 on HOST/USER: $(uname -Ia)/$USER"
echo "**                                   Repo:" $MyRepo
echo "**                  Application WorkSpace:" $MyWorkSpace
echo "**                            Common Repo:" $CommonSrcDir 
echo "**                                 Branch:" $branch
echo "**                                App Tag:" $MyTag 

git clone -b $branch $MyRepo  2>&1
cd $CommonSrcDir 

echo "Show all Refs"
git show-ref

if [ ! -z $MyTag ]; then
   git config  advice.detachedHead false 
   git checkout $MyTag > tag 2>&1
   cat tag 
   rm tag
fi

echo "Show status"
git status 

