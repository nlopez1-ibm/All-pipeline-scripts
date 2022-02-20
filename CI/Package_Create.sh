# Sample script to package the artifacts in a DBB output folder and tars them with a yaml manifest

. $HOME/.profile
package=/u/nlopez/All-pipeline-scripts/utilities/package.groovy

echo "**************************************************************"
echo "**     Started: Packaging on HOST/USER: $(uname -Ia) $USER"
echo "**                             WorkDir:" $1
echo "**                       DBB WorkSpace:" $2
echo "**                                 App:" $3
echo "**                             Version:  1" 
echo "**                      Package Script:" $package
 

groovyz $package\
  -workDir      $1\
  -workSpace    $2\
  -application  $3\
  -version      1\
  -s gitSourceUrl-TBD\
  -g git@github.ibm.com:Nelson-Lopez1/a-dummy-repo.git\
  -x gitSourceBranch-TBD\
  -y gitBuildBranch-TBD\
  -b 12345678\
  -n 1\
  -u none.com
