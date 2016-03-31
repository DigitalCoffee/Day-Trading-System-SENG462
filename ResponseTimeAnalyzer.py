#!/usr/bin/python

import sys
import re

output_regex = r"(COMMAND TOOK TOO LONG!!!!!!)?.*:\s*(\d+\.\d*)\s*msecs"

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
        if m and m.group(2):
            if m.group(1):
                slow_commands = slow_commands + 1
            total_msecs = total_msecs + float(m.group(2))
            total_commands = total_commands + 1

    print "Average response time for " + str(total_commands) + ":"
    print str(total_msecs / total_commands)
    print "Late commands: " + str(slow_commands)


if __name__ == "__main__":
   main(sys.argv)
