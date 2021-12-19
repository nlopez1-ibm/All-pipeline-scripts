OLD Not used - save for refer 
#! /bin/sh                                                                      
## Clone and DBB Build Script v1.4 (njl)
## Required Args: 
##    buildMode  - can be any valid DDB buildMode - fullBuild, impactBuild, scanOnly
##    ucdver     - a version # used to tag the output for UCD deploy.groovy like  AZV_\$(Build.BuildId)
##    Repo       - the repo url like git@github.ibm.com:yourid/your_repo.git
######

## check for the input arg
if [ ! "$#" -gt 2 ]; then ; echo "Error Missing Arg (see comments in script)" >&2 ;  exit 1 ; fi  

## Setup Environment
export _BPX_SHAREAS=NO    ; export _BPXK_AUTOCVT=ON  ; export _CEE_RUNOPTS="FILETAG(AUTOCVT,AUTOTAG) POSIX(ON)" 
export _TAG_REDIR_ERR=txt ; export _TAG_REDIR_IN=txt ; export _TAG_REDIR_OUT=txt 
export JAVA_HOME=/usr/lpp/java/J8.0_64  ; export DBB_HOME=/u/nlopez/IBM/dbb ; export GROOVY_HOME=$DBB_HOME/groovy-2.4.12   
export GIT_SHELL=/var/rocket/bin/bash   ; export GIT_EXEC_PATH=/var/rocket/libexec/git-core 
export GIT_Path=/var/rocket/bin         ; export PERL5LIB=/var/rocket/share/perl/5.24.1:$PERL5LIB 
export GIT_TEMPLATE_DIR=/var/rocket/share/git-core/templates 
export PATH=$JAVA_HOME/bin:$GROOVY_HOME/bin:$DBB_HOME/bin:$GIT_Path:$PATH 

buildMode=$1 
ucdver=$2
AzRepo="$3" ;  App=$(basename $AzRepo) ;   App=${App%.*}
# support 2 repo folder structures
#  1  for Azure style repo in my demo with not workspace
#  2  for the IDz repo demo that has a workspace  (this mode is defined by non-null arg 4) and hard code app name for now 
workSpace=$HOME/Azure-Workspace ;  if [ ! -d $workSpace ] ; then ;  mkdir $workSpace ; fi ; cd $workSpace
if [ ! -z $4 ]  ; then ; workSpace=$HOME/$App ; App="Mortgage-SA-DAT" ; cd $HOME; fi 

 
 

hlq=NLOPEZ.AZURE                                                          
dbblogs=$PWD/dbblogs                                                    
BuildCmd=$HOME/Azure-zAppBuild/build.groovy 

###### DBB
echo "** "
echo "***" 
echo "**   DBB Reset of collection Build ... $App"                                                                                  
groovyz $BuildCmd -a $App  -w $workSpace -o $dbblogs -h $hlq   -uver $ucdver   --$buildMode

 