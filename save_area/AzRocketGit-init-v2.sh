#!/bin/sh
## Az/DBB Demo- Git clone HTTPS Support v1.4 (njl)
##            - Support azure projects with blanks (must encode the var in the pipelie 
##              with quotes like  "$(var)"     


. $HOME/.profile

MyRepo="$1"
MyWorkDir="$2" ; mkdir -p "$MyWorkDir"  ; cd "$MyWorkDir"
branch=$3

# Strip the prefix of the branch for cloning
branch=${branch##*"refs/heads/"}   

#secret format https://<user>:<pat>@<domain>
MyCred=$4
 
workSpace=$(basename "$MyRepo") ;workSpace=${workSpace%.*}
echo "**************************************************************"
echo "**     Started:  Rocket-Git Clone on HOST/USER: $(uname -Ia)/$USER"
echo "**                                   Repo:" $MyRepo
echo "**                                WorkDir:" $PWD
echo "**                              WorkSpace:" $workSpace
echo "**                                 Branch:" $3  "->" $branch

# Prep cred store with azure vars
git config --global credential.helper store
echo $MyCred > ~/.git-credentials 


git clone -b $branch "$MyRepo"  2>&1
cd  "$workSpace"

echo "Show status"
git status 

echo "Show all Refs"
git show-ref

# remove the credentials 
rm ~/.git-credentials
