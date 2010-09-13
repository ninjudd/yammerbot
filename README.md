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
