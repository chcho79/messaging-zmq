#!/bin/sh
PROJECT_ROOT=$(pwd)
echo $PROJECT_ROOT
cd ../../../../
REPO_ROOT=$(pwd)
cd EMF/java/edgex-emf
EMF_ROOT=$(pwd)

#start install EMF
cd $EMF_ROOT
./build.sh

#start package
cd $PROJECT_ROOT
mvn install -U
echo "done"
