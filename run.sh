#!/bin/bash

# Provide your Netlink id as the first argument
if [ -n "$1" ]
then
# Add the -copy argument to compile the code and move it to their respective machines
  if [ -n "$2" ] && [ "$2" = "-copy" ]
  then
    make
    scp audit.jar $1@b135.seng.uvic.ca:/seng/scratch/group5/
    scp transaction.jar $1@b145.seng.uvic.ca:/seng/scratch/group5/
    scp http.jar $1@b130.seng.uvic.ca:/seng/scratch/group5/
  fi
  ssh $1@b135.seng.uvic.ca "java -jar /seng/scratch/group5/audit.jar &"
  ssh $1@b145.seng.uvic.ca "java -jar /seng/scratch/group5/transaction.jar &"
  ssh $1@b130.seng.uvic.ca "java -jar /seng/scratch/group5/http.jar"
else
  echo "Usage: sh run.sh <netlink id> [-copy]"
  echo "Using the -copy flag will compile the code and move the jar files to their respective machines in /seng/scratch/group5"
fi
