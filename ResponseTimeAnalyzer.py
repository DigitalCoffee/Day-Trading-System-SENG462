#!/usr/bin/python

import sys
import re

output_regex = r"(COMMAND TOOK TOO LONG!!!!!!)?.*\[\d+\]\s*(\w+).*:\s*(\d+\.\d*)\s*msecs"

commands = {
        "ADD": [],
        "QUOTE": [],
        "BUY": [],
        "SELL": [],
        "COMMIT_BUY": [],
        "COMMIT_SELL": [],
        "CANCEL_BUY": [],
        "CANCEL_SELL": [],
        "SET_BUY_AMOUNT": [],
        "SET_SELL_AMOUNT": [],
        "SET_BUY_TRIGGER": [],
        "SET_SELL_TRIGGER":[],
        "CANCEL_SET_BUY": [],
        "CANCEL_SET_SELL": [],
        "DISPLAY_SUMMARY": [],
        "DUMPLOG": []
        }

def main(argv):
    total_msecs = 0
    total_commands = 0
    slow_commands = 0
    if not sys.argv[1]:
        print "Please provide the path to the file."
        sys.exit(0)
    fo = open(argv[1], "r+")
    for line in fo:
        m = re.match(output_regex, line.strip())
        if m and m.group(3):
            if m.group(1):
                slow_commands = slow_commands + 1
            commands[m.group(2)].append(float(m.group(3)))
            total_msecs = total_msecs + float(m.group(3))
            total_commands = total_commands + 1

    print "Average response time for " + str(total_commands) + ": " + str(total_msecs / total_commands) + "msecs"
    print "Late commands: " + str(slow_commands)

    for k,v in commands.iteritems():
        if len(v) > 0:
            print k + " Average: " + str(sum(v)/len(v)) + ", Min: " + str(min(v)) + ", Max: " + str(max(v))


if __name__ == "__main__":
   main(sys.argv)
