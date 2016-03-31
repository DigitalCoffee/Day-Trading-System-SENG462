#!/usr/bin/python

import sys
import re

log_regex = r"(\w+),(\d+),(\w+),(\d+),(.+),(.*),(.*),(.*),(.*),(.*)"
log_types = ["userCommand",
                  "quoteServer",
                  "accountTransaction",
                  "systemEvent",
                  "errorEvent",
                  "debugEvent"]

def tagIT(tag, innerStr):
    return "<" + tag + ">" + innerStr + "</" + tag + ">\n"

def main(argv):

    if not sys.argv[1]:
        print "Please provide the path to the log file."
        sys.exit(0)
    fo = open("./testLOG", "w+")
    fo.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n<log>\n")
    with open(argv[1]) as infile:
        for line in infile:
            m = re.match(log_regex, line.strip())
            if not m.group(1) or m.group(1) not in log_types:
                print "INVALID LOG: " + line
                continue
            log = tagIT("timestamp", m.group(2)) + tagIT("server", m.group(3)) + tagIT("transactionNum", m.group(4))
            if m.group(1) == "userCommand" or m.group(1) == "systemEvent":
                log = log + tagIT("command", m.group(5))
                if m.group(6):
                    log = log + tagIT("username", m.group(6))
                if m.group(8):
                    log = log + tagIT("stockSymbol", m.group(8))
                if m.group(9):
                    log = log + tagIT("filename", m.group(9))
                if m.group(7):
                    log = log + tagIT("funds", m.group(7))
                if m.group(1) == "errorEvent" and m.group(10):
                    log = log + tagIT("errorMessage", m.group(10))
                elif m.group(1) == "errorEvent" and m.group(10):
                    log = log + tagIT("debugEvent", m.group(10))
            elif m.group(1) == "quoteServer":
                log = log + tagIT("price", m.group(5))
                log = log + tagIT("stockSymbol", m.group(6))
                log = log + tagIT("username", m.group(7))
                log = log + tagIT("quoteServerTime", m.group(8))
                log = log + tagIT("cryptokey", m.group(9))
            elif m.group(1) == "accountTransaction":
                log = log + tagIT("action", m.group(5))
                log = log + tagIT("username", m.group(6))
                log = log + tagIT("funds", m.group(7))
            log = tagIT(m.group(1), log)
            fo.write(log)
    fo.write("</log>")
    fo.close()

if __name__ == "__main__":
   main(sys.argv)
