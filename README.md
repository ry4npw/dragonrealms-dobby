# dragonrealms-dobby

Dobby is a Java-based scripting proxy for [DragonRealms](https://www.play.net/dr/) (aka DR). The idea is that it works with, rather than replacing your client. You probably heard of the [Lich Project](https://lichproject.org), this does something like that.

To date, dobby has been tested on Mac with the Avalon client.

## Why the name?

Dobby is a house elf (creatures that are sworn to serve their master wizards). This proxy is supposed to do a similar function, help you while playing DR.

## Why Java?

I decided to write dobby in Java for two reasons:

1. Java has pretty good cross-platform support; dobby should (one day) run on Windows, Mac OS, and Linux.
2. My personal preference, and hey, this is really for me.

## How it works

Dobby is a telnet proxy that sits in-between your client and the DR server. This mean that dobby can intercept and modify your commands en-route to the server (aliasing) or send commands on your behalf (scripting).

<pre>
client <-> dobby <-> dr.simutronics.net
</pre>

Dobby does not change your client experience (highlights, windows, etc) directly, but will add features to your client. You interact with dobby by typing special commands that start with a semicolon (;).

## How do I run it?

### Windows

Dobby is currently untested on Windows, and may require some tweaks to run. I also have not tried it with XML streams that the StormFront and Genie clients use, but maybe one day...

### Linux / Mac OS X

Dobby works great with Avalon on Mac.

#### Startup

1. Download dobby-{version}.jar and place it in your ~/Documents/dobby directory.
2. Redirect your DR traffic through dobby via `sudo vi /etc/hosts` and add the entry:

	```bash
	127.0.0.1 dr.simutronics.net
	```

3. Open Terminal.app, and run the following commands. _Note: You will need to enter your password after typing the `sudo` command._

	```bash
	$ cd ~/Documents/dobby
	$ java -jar dobby-{version}.jar
	```

##### Advanced Startup

You can connect to an upstream proxy. This is useful for getting around firewalls:
```bash
$ java -jar dobby-{version}.jar {localPort} {remoteIP} {remotePort}
$ java -jar dobby-{version}.jar 4901 upstream.proxy.server.com 80
```

You can also have dobby connect through a SOCKS 5 proxy:
```bash
$ java -DsocksProxyHost=socks.example.com -DsocksProxyPort=1080 -jar dobby-{version}.jar
```

#### Shutdown

To shutdown dobby, you can close your Terminal window or click on the Terminal window and press control + c on your keyboard.

## How do I use it?

Once running, you interact with dobby by sending commands to the server. Dobby will intercept any command you send that starts with a semicolon (;).

### Dobby commands

####List
* `;list` lists all currently active scripts, this includes running and paused scripts.

####Pause
* `;pause` pauses all currently running scripts.
* `;pause #` pauses the specified script, where # is the numeric identifier from `;list`.

####Repeat
* `;repeat #` resends the last # commands (up to 10) that you typed to the server. This is done by the command queue (the same queue used for all executing scripts), which will automatically handle RT and type ahead lines. ;repeat sends commands blindly, and does not wait for other events/triggers before sending commands.

    ```
    >order 2
    The attendant says, "You can purchase a massive coal nugget for 212 Kronars.  Just order it again and we'll see it done!"
    >order 2
    The attendant takes some coins from you and hands you a massive coal nugget.
    >stow nugget
    You put your nugget in your brewer's knapsack.
    >;repeat 3
    ```

####Resume
* `;resume` resumes all currently paused scripts.
* `;resume #` resumes the specified script, where # is the numeric identifier from `;list`.

####Stop
* `;stop` stops all currently running scripts.
* `;stop #` stops the specified script, where # is the numeric identifier from `;list`.

### Scripting

Dobby will support StormFront (identified by the .sf extension) scripts that are in your Documents/dobby/ folder. Dobby uses the extension to determine which type of script you wish to run, in the future it is planned to support additional formats. The goal is to work out some bugs with StormFront scripts first, then develop a Genie interpreter that will include a flat file for global variables. Dobby is not limited to running just one or two script at a time, but executing too many scripts could cause unexpected behavior with RT issues and competion for sending commands, this is the scripter/user's issue to deal with.

Dobby uses a command queue to send all dobby-generated commands (user input is still passed through to the server immediately). The command queue is a FIFO queue that automatically "WAIT"s between commands and for roundtime. If a command fails because of type ahead or roundtime conditions, it generally will retry that command again. If your script is dependent on a command to be executed within a certain amount of time and the command queue is waiting on a long RT from some other action, your script may fail.

`;look.sf table` will run the [look.sf](https://github.com/ry4npw/dragonrealms-dobby/blob/master/src/test/resources/look.sf) script (assuming it's in the right place!) with table as the %1 variable. In your client, you will end up seeing something like this:

```
>;look.sf table
dobby [look.sf: START]
dobby [look.sf: look in table]
There is nothing in there.
>
dobby [look.sf: look on table]
On the long stone table you see some flight glue, a band, a band, and a glass of potato peel vodka.
>
dobby [look.sf: look under table]
There is nothing under there.
>
dobby [look.sf: look behind table]
dobby [look.sf: END, completed in 593ms]
There is nothing behind there.
```

### Future

I have plans to make Dobby a lot more powerful, working across sessions on the same machine. Here are some of my ideas in no particular order:

* timed/scheduled commands (every {time} {unit} {command})

    ```
    >;every 91 seconds PREDICT WEATHER
    ```

* support sending commands for other characters on the same computer (ask {toon} to {command})

    ```
    >;ask weensie to TEACH PADHG FORGING
    ```

* support other scripting languages such as running Genie scripts, or maybe other interpreted languages such as JavaScript and Python.

* add percentages to HEALTH/MANA. also add numerals to appraisals and combat messages.

* Create a StreamListener to parse GS* variables from the [SIMU-PROTOCOL](https://github.com/sproctor/warlock-gtk/blob/master/docs/SIMU-PROTOCOL)

* XML parsing, including exposing those elements as variables to scripts

* (post parsing) a RESTful interface to return real-time character information, this could make it easy to create a single-page HTML application to add things like health/spirit/stamina/fatigue bars to clients such as Avalon. Or by combining dobby services you could create a display of all your friends, their health/spirit/stamina/fatigue, location, and etc. User interfaces could also be done in the browser for things like script writing/management, or even for mapping programs.

* auto-mapping, support for Genie map repository, and an HTML map interface for movement.
