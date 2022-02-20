# Sample test script to package a DBB Build  for staging into artifactory
  
# The Jenkinsfile in this folder shows how to implement this script in Jenkins
# Jenkins will use ortqq/application-conf/package.properties not the environment variable shown here 
clear 

# makes sure there are no trailing spaces after the \

groovyz $HOME/Garanti-zAppBuild/package.groovy\
  -w /u/nlopez/tmp/jenkins-mylocal-server/workspace/pocPipeline/dbb-logs-213/build.20210319.040454.004\
  -a ortqq\
  -v "2.21.0"\
  -s gitSourceUrl-TBD\
  -g git@github.ibm.com:Nelson-Lopez1/ortqq.git\
  -x gitSourceBranch-TBD\
  -y gitBuildBranch-TBD\
  -b 12345678\
  -n P092259\
  -u ARTIFACTORY.COM
   
  
  
