#!/bin/bash

# Provide your Netlink id as the first argument
if [ -n "$1" ] || [ -n "$2" ] || [ -n "$3" ]
then
    if [ -n "$4" ]
    then
        sshpass -p $2 scp jar/workload.jar $1@b130.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/workloadrunner.jar $1@b131.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/workloadrunner.jar $1@b132.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/workloadrunner.jar $1@b133.seng.uvic.ca:/seng/scratch/group5/
        #134 is the NGINX load balancer
        sshpass -p $2 scp jar/workloadrunner.jar $1@b135.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/http.jar $1@b136.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/http.jar $1@b137.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/http.jar $1@b138.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/http.jar $1@b139.seng.uvic.ca:/seng/scratch/group5/
        #140 is the PostgreSQL database
        sshpass -p $2 scp jar/http.jar $1@b141.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/transaction.jar $1@b142.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/transaction.jar $1@b143.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/transaction.jar $1@b144.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/transaction.jar $1@b145.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/transaction.jar $1@b146.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/audit.jar $1@b147.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/quote.jar $1@b148.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/quote.jar $1@b149.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/db.jar $1@b140.seng.uvic.ca:/seng/scratch/group5/ #CHANGE LATER
        sshpass -p $2 scp jar/trigger.jar $1@b150.seng.uvic.ca:/seng/scratch/group5/
        sshpass -p $2 scp jar/naming.jar $1@b153.seng.uvic.ca:/seng/scratch/group5/
    fi
    #Clear DB for run
    sshpass -p $2 ssh $1@b140.seng.uvic.ca "java -cp /seng/scratch/group5/:/seng/scratch/group5/db.jar db"
    sshpass -p $2 ssh $1@b147.seng.uvic.ca "rm /seng/scratch/group5/log_*.txt"
    sshpass -p $2 ssh $1@b134.seng.uvic.ca "rm /seng/scratch/group5/installed/nginx/logs/*.log"

    #Run Naming -> Audit -> DB & QuoteCache -> Transactions -> HTTP's -> Workload Runners -> Workload Generator
    gnome-terminal -e "sshpass -p $2 ssh $1@b153.seng.uvic.ca \"java -jar /seng/scratch/group5/naming.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b134.seng.uvic.ca \"/seng/scratch/group5/installed/nginx/sbin/nginx\"" &

    read -p "Press any key when ready" c
    gnome-terminal -e "sshpass -p $2 ssh $1@b131.seng.uvic.ca \"java -jar /seng/scratch/group5/workloadrunner.jar > run1.txt\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b132.seng.uvic.ca \"java -jar /seng/scratch/group5/workloadrunner.jar > run2.txt\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b133.seng.uvic.ca \"java -jar /seng/scratch/group5/workloadrunner.jar > run3.txt\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b135.seng.uvic.ca \"java -jar /seng/scratch/group5/workloadrunner.jar > run4.txt\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b147.seng.uvic.ca \"java -jar /seng/scratch/group5/audit.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b140.seng.uvic.ca \"java -jar /seng/scratch/group5/db.jar\"" & #CHANGE LATER

    read -p "Press any key when ready" c
    gnome-terminal -e "sshpass -p $2 ssh $1@b148.seng.uvic.ca \"java -jar /seng/scratch/group5/quote.jar\"" &
    #gnome-terminal -e "sshpass -p $2 ssh $1@b149.seng.uvic.ca \"java -jar /seng/scratch/group5/quote.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b150.seng.uvic.ca \"java -jar /seng/scratch/group5/trigger.jar\"" &

    read -p "Press any key when ready" c
    gnome-terminal -e "sshpass -p $2 ssh $1@b142.seng.uvic.ca \"java -jar /seng/scratch/group5/transaction.jar 1\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b143.seng.uvic.ca \"java -jar /seng/scratch/group5/transaction.jar 2\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b144.seng.uvic.ca \"java -jar /seng/scratch/group5/transaction.jar 3\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b145.seng.uvic.ca \"java -jar /seng/scratch/group5/transaction.jar 4\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b146.seng.uvic.ca \"java -jar /seng/scratch/group5/transaction.jar 5\"" &

    read -p "Press any key when ready" c
    gnome-terminal -e "sshpass -p $2 ssh $1@b136.seng.uvic.ca \"java -jar /seng/scratch/group5/http.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b137.seng.uvic.ca \"java -jar /seng/scratch/group5/http.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b138.seng.uvic.ca \"java -jar /seng/scratch/group5/http.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b139.seng.uvic.ca \"java -jar /seng/scratch/group5/http.jar\"" &
    gnome-terminal -e "sshpass -p $2 ssh $1@b141.seng.uvic.ca \"java -jar /seng/scratch/group5/http.jar\"" &

    read -p "Press any key when ready" c
    sshpass -p $2 ssh $1@b130.seng.uvic.ca "java -jar /seng/scratch/group5/workload.jar $3 5 > run0.txt"
    sshpass -p $2 ssh $1@b134.seng.uvic.ca "/seng/scratch/group5/installed/nginx/sbin/nginx -s quit"
else
  echo "Usage: sh run.sh <netlink id> <password?!?!> <workload file in netdrive home> <optional parameter to copy jar files>"
  echo "Additional libraries are included in the jar files"
fi
