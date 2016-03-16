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

# FOR LOCAL TESTING ONLY: Quote cost is always the same value
QUOTE_COST = 14.99

def main(argv):
    users = {}
    accounts = {}
    buys = {}
    sells = {}
    errors = []     # Commands that cannot complete because they violate their process in some way
    faults = []         # Correspond to transaction failures to to mismatched prices. Can fluctuate based on quote values
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
        print "Please provide the path to the workload file."
        print "You may add up to two additional arguments:"
        print "2nd: prints additional output (# of commands for each user/stock, commands that cause errors)"
        print "3rd: prints out additional info for testing on a local machine (All quotes are $14.99, no time checking)"
        sys.exit(0)
    fo = open(argv[1], "r+")
    for line in fo:
        line = line.strip()
        m = re.match(command_regex, line.strip())
        commands[m.group(1)] = commands[m.group(1)] + 1

        # User id is guaranteed to be group 2 if command is not Dumplog
        if m.group(1) != "DUMPLOG":

            # Increase number of commands for corresponding user
            if m.group(2) not in users:
                users[m.group(2)] = 1
            else:
                users[m.group(2)] = users[m.group(2)] + 1

            # ADD command adds user into system
            if m.group(1) == "ADD" and m.group(2) not in accounts:
                accounts[m.group(2)] = {}
                accounts[m.group(2)]["money"] = float(m.group(3))
            # System error if user not in system
            elif m.group(1) != "ADD" and m.group(2) not in accounts:
                errors.append(line)
                continue
            # User already exists, add more money to account
            elif m.group(1) == "ADD" and m.group(2) in accounts:
                accounts[m.group(2)]["money"] = accounts[m.group(2)]["money"] + float(m.group(3))

            # Buys are added to a stack if the user has enough money equal to:
            # QUOTE_COST * max integer value of shares that can be bought for requested price
            if m.group(1) == "BUY":
                # Tracks the attempt to buy a stock
                if m.group(3) not in accounts[m.group(2)]:
                    accounts[m.group(2)][m.group(3)] = 0
                # Check if user has enough money then either push to stack or record fault
                price = int( float(m.group(4))/QUOTE_COST ) * QUOTE_COST
                if price <= 0.0 or accounts[m.group(2)]["money"] < price:
                    faults.append(line)
                else:
                    if m.group(2) not in buys:
                        buys[m.group(2)] = []
                    buys[m.group(2)].append([m.group(3), price])

            # Commits and Cancels pop from the stack
            # Commits will update the user's account
            if m.group(1) == "COMMIT_BUY" or m.group(1) == "CANCEL_BUY":
                if m.group(2) not in buys:
                    errors.append(line)
                    continue
                elif len(buys[m.group(2)]) < 1:
                    faults.append(line)
                else:
                    cur = buys[m.group(2)].pop()
                    if m.group(1) == "COMMIT_BUY":
                        if accounts[m.group(2)]["money"] < cur[1]:
                            faults.append(line)
                        elif accounts[m.group(2)]["money"] >= cur[1]:
                            accounts[m.group(2)]["money"] = accounts[m.group(2)]["money"] - cur[1]
                            if cur[0] not in accounts[m.group(2)]:
                                accounts[m.group(2)][cur[0]] = int(cur[1] / QUOTE_COST)
                            else:
                                accounts[m.group(2)][cur[0]] = accounts[m.group(2)][cur[0]] + int(cur[1] / QUOTE_COST)

            # Sells are added to a stack if the user has enough of the stock they wish to sell
            if m.group(1) == "SELL":
                # There has never been a corresponding buy-type command before the sell command was sent
                if m.group(3) not in accounts[m.group(2)]:
                    errors.append(line)
                    continue
                amount = int(float(m.group(4)))
                if accounts[m.group(2)] < amount:
                    faults.append(line)
                else:
                    if m.group(2) not in sells:
                        sells[m.group(2)] = []
                    sells[m.group(2)].append([m.group(3), amount])

            # Commits and Cancels pop from the stack
            # Commits will update the user's account
            if m.group(1) == "COMMIT_SELL" or m.group(1) == "CANCEL_SELL":
                if m.group(2) not in sells:
                    errors.append(line)
                    continue
                elif len(sells[m.group(2)]) < 1:
                        faults.append(line)
                else:
                    cur = sells[m.group(2)].pop()
                    if m.group(1) == "COMMIT_SELL":
                        if accounts[m.group(2)][cur[0]] < cur[1]:
                            faults.append(line)
                        elif accounts[m.group(2)][cur[0]] >= cur[1]:
                            accounts[m.group(2)][cur[0]] = accounts[m.group(2)][cur[0]] - cur[1]
                            accounts[m.group(2)]["money"] = accounts[m.group(2)]["money"] + (cur[1] * QUOTE_COST)

            #if m.group(1) == "SET_BUY_AMOUNT":

            #if m.group(1) == "":

        # Increment the number of commands related to the corresponding stock symbol
        if m.group(1) in stock_commands:
            if m.group(3) not in stocks:
                stocks[m.group(3)] = 1
            else:
                stocks[m.group(3)] = stocks[m.group(3)] + 1


    print "Stats for " + str(len(users)) + " workload:"
    print "NOTE: triggers are not included in error count, faults, and potential final account info"

    num_cmds = 0
    for command in commands.iterkeys():
        num_cmds = num_cmds + commands[command]
    print "COMMANDS: " + str(num_cmds)
    for command in commands.iterkeys():
        print "\t" + command + ": " + str(commands[command])

    if len(sys.argv) > 2:
        print "\nUNIQUE USERS:"
        for user in users.iterkeys():
            print "\t" + user + ": " + str(users[user])

    print "\nUNIQUE STOCK SYMBOLS: " + str(len(stocks))
    if len(sys.argv) > 2:
        for stock in stocks.iterkeys():
            print "\t" + stock + ": " + str(stocks[stock])

    # Print additional info for local testing
    if len(sys.argv) > 3:
        print "\nPOTENTIAL ACCOUNT DETAILS AFTER COMPLETION:"
        print "NOTE: This does not account for timing of commands"
        print "This section is only really useful for local testing a small number of users with few commands (<1000?)"
        for user in accounts.iterkeys():
            print user
            for item in accounts[user].iterkeys():
                print "\t" + item + ": " + str(accounts[user][item])

        print "\nFAULTS: " + str(len(faults))
        for fault in faults:
            print fault

    print "\nMINIMUM ERROR COUNT: " + str(len(errors))
    if len(sys.argv) > 2:
        for error in errors:
            print error

if __name__ == "__main__":
   main(sys.argv)
