#!/bin/sh
## IBM Demo script git clone v1.3 (nlopez)
## for faster cloning of features consider --depth 1. All others other need the history

. $HOME/.profile

MyRepo=$1
MyWorkDir=$2 
workSpace=$3
branch=$4
   
# v1.3- supports branch names or tags. Azure branch names are prefixed with 'refs/heads/...'. 
# If a prefix is found, this assumes its a branch name and strips it. Else its a tag.  
label="   Tag"
if [[ $branch = *"refs/heads/"* ]]; then 
    branch=${branch##*"refs/heads/"}
    label="Branch"
fi   
 

# for common repos switch to the main app repo's workSpace previously created by the first clone step
if [ -d $MyWorkDir/$workSpace ]; then
   cd $MyWorkDir/$workSpace
else 
    mkdir -p $MyWorkDir
    cd $MyWorkDir
fi 
 
echo "**************************************************************"
echo "**     Started:  Rocket-Git Clone on HOST/USER: $(uname -Ia)/$USER"
echo "**                                   Repo:" $MyRepo
echo "**                          WorkDir (pwd):" $PWD
echo "**                              WorkSpace:" $workSpace
echo "**                                 $label:" $branch
echo "**\n"

git config --global  advice.detachedHead false
git clone -b $branch $MyRepo  2>&1