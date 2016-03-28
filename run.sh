#!/bin/bash

# Provide your Netlink id as the first argument
if [ -n "$1" ] || [ -n "$2" ]
then
    if [ -n "$3" ]
    then
        scp jar/naming.jar $1@b153.seng.uvic.ca:/seng/scratch/group5/
        scp jar/audit.jar $1@b135.seng.uvic.ca:/seng/scratch/group5/
        scp jar/db.jar $1@b140.seng.uvic.ca:/seng/scratch/group5/
        scp jar/quote.jar $1@b148.seng.uvic.ca:/seng/scratch/group5/
        scp jar/transaction.jar $1@b145.seng.uvic.ca:/seng/scratch/group5/
        scp jar/http.jar $1@b150.seng.uvic.ca:/seng/scratch/group5/
        scp jar/workload.jar $1@b132.seng.uvic.ca:/seng/scratch/group5/
        scp jar/workloadslave.jar $1@b130.seng.uvic.ca:/seng/scratch/group5/
        scp jar/workloadslave.jar $1@b131.seng.uvic.ca:/seng/scratch/group5/
    fi
    ssh $1@b140.seng.uvic.ca "java -cp /seng/scratch/group5/:/seng/scratch/group5/lib/postgresql-9.4.1208.jar db"
    gnome-terminal -e "ssh $1@b153.seng.uvic.ca \"java -jar /seng/scratch/group5/naming.jar\"" &
    gnome-terminal -e "ssh $1@b135.seng.uvic.ca \"java -jar /seng/scratch/group5/audit.jar\"" &
    gnome-terminal -e "ssh $1@b140.seng.uvic.ca \"java -jar /seng/scratch/group5/db.jar\"" &
    gnome-terminal -e "ssh $1@b148.seng.uvic.ca \"java -jar /seng/scratch/group5/quote.jar\"" &
    gnome-terminal -e "ssh $1@b145.seng.uvic.ca \"java -jar /seng/scratch/group5/transaction.jar\"" &
    gnome-terminal -e "ssh $1@b150.seng.uvic.ca \"java -jar /seng/scratch/group5/http.jar\"" &
    gnome-terminal -e "ssh $1@b130.seng.uvic.ca \"java -jar /seng/scratch/group5/workloadslave.jar\"" &
    gnome-terminal -e "ssh $1@b131.seng.uvic.ca \"java -jar /seng/scratch/group5/workloadslave.jar\"" &
    gnome-terminal -e "ssh $1@b132.seng.uvic.ca \"java -jar /seng/scratch/group5/workload.jar $2 3\"" &
else
  echo "Usage: sh run.sh <netlink id> <workload file in netdrive home> <optional parameter to copy jar files>"
  echo "Additional libraries are not copied because they are already on the machines"
fi
