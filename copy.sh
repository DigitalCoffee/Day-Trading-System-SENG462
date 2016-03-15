#!/bin/bash

# Provide your Netlink id as the first argument
if [ -n "$1" ]
then
    scp jar/naming.jar $1@b153.seng.uvic.ca:/seng/scratch/group5/
    scp jar/audit.jar $1@b135.seng.uvic.ca:/seng/scratch/group5/
    scp jar/quote.jar $1@b148.seng.uvic.ca:/seng/scratch/group5/
    scp jar/transaction.jar $1@b145.seng.uvic.ca:/seng/scratch/group5/
    scp jar/http.jar $1@b150.seng.uvic.ca:/seng/scratch/group5/
    scp jar/workload.jar $1@b149.seng.uvic.ca:/seng/scratch/group5/
else
  echo "Usage: sh run.sh <netlink id>"
fi
