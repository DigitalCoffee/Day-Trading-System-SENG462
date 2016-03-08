#!/usr/bin/python

import sys
import re

command_regex = r"\[\d+\]\s*(\w+),([^,]+),?([^,]+)?,?([^,]+)?"
stock_commands = ["QUOTE",
                  "BUY",
                  "SELL",
                  "SET_BUY_AMOUNT",
                  "SET_SELL_AMOUNT",
                  "SET_BUY_TRIGGER",
                  "SET_SELL_TRIGGER",
                  "CANCEL_SET_BUY",
                  "CANCEL_SET_SELL"]

def main(argv):
    users = {}
    stocks = {}
    commands = {
        "ADD": 0,
        "QUOTE": 0,
        "BUY": 0,
        "SELL": 0,
        "COMMIT_BUY": 0,
        "COMMIT_SELL": 0,
        "CANCEL_BUY":0,
        "CANCEL_SELL":0,
        "SET_BUY_AMOUNT": 0,
        "SET_SELL_AMOUNT": 0,
        "SET_BUY_TRIGGER": 0,
        "SET_SELL_TRIGGER":0,
        "CANCEL_SET_BUY": 0,
        "CANCEL_SET_SELL": 0,
        "DISPLAY_SUMMARY": 0,
        "DUMPLOG": 0
        }

    if not sys.argv[1]:
        print "Please provide the path to the workload file"
        sys.exit(0)
    fo = open(argv[1], "r+")
    for line in fo:
        m = re.match(command_regex, line.strip())
        commands[m.group(1)] = commands[m.group(1)] + 1
        if m.group(1) != "DUMPLOG":
            if m.group(2) not in users:
                users[m.group(2)] = 1
            else:
                users[m.group(2)] = users[m.group(2)] + 1
        if m.group(1) in stock_commands:
            if m.group(3) not in stocks:
                stocks[m.group(3)] = 1
            else:
                stocks[m.group(3)] = stocks[m.group(3)] + 1

    print "Stats for " + str(len(users)) + " workload:"

    print "COMMANDS:"
    print "\tADD: " + str(commands["ADD"])
    print "\tQUOTE: " + str(commands["QUOTE"])
    print "\tBUY: " + str(commands["BUY"])
    print "\tSELL: " + str(commands["SELL"])
    print "\tCOMMIT_BUY: " + str(commands["COMMIT_BUY"])
    print "\tCOMMIT_SELL: " + str(commands["COMMIT_SELL"])
    print "\tCANCEL_BUY: " + str(commands["CANCEL_BUY"])
    print "\tCANCEL_SELL: " + str(commands["CANCEL_SELL"])
    print "\tSET_BUY_AMOUNT: " + str(commands["SET_BUY_AMOUNT"])
    print "\tSET_SELL_AMOUNT: " + str(commands["SET_SELL_AMOUNT"])
    print "\tSET_BUY_TRIGGER: " + str(commands["SET_BUY_TRIGGER"])
    print "\tSET_SELL_TRIGGER: " + str(commands["SET_SELL_TRIGGER"])
    print "\tCANCEL_SET_BUY: " + str(commands["CANCEL_SET_BUY"])
    print "\tCANCEL_SET_SELL: " + str(commands["CANCEL_SET_SELL"])
    print "\tDISPLAY_SUMMARY: " + str(commands["DISPLAY_SUMMARY"])
    print "\tDUMPLOG: " + str(commands["DUMPLOG"]) + "<-- SHOULD BE 1"

    print "UNIQUE USERS:"
    print users

    print "UNIQUE STOCK SYMBOLS: " + str(len(stocks))
    print stocks

if __name__ == "__main__":
   main(sys.argv)
