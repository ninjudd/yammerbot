An irc bot for Yammer.

Yammerbot will sit in various channels on a private company irc server and request that
users authorize their username with Yammer. All messages from authorized users will be
logged to a Yammer group (#foo -> foo-irc).

## Installation

Yammerbot is written in Clojure and uses the [cake build tool](http://github.com/ninjudd/cake).
To build a standalone executable:

    git clone git://github.com/ninjudd/yammerbot.git
    cd yammerbot
    cake bin

Then you can start yammerbot with:

    ./yammerbot irc.yourcompany.com '#channel' -p="yourpassword"

## Configuration

If you don't want your irc messages to appear in your Yammer feed, you can authorize yammerbot with an alternate email address. Most companies permit a suffix like yourname+irc@yourcorp.com. Otherwise, ask a sysadmin nicely to set up an email alias for you and use that.