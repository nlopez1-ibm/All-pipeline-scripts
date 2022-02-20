move into zapbuild build groovy 


#! /bin/sh
##  SAMPLE     - publish a package 
##  Pre-Req:   - Run DBB Build, then Package.sh  
##  Dep:       - Rocket curl, Package.groovy, ArtifactoryCurlHelpers.groovy  
clear 
groovyz $HOME/All-pipeline-scripts/utilities/publish.groovy