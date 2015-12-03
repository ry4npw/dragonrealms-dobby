# dragonrealms-dobby

Dobby is a Java-based scripting proxy for [DragonRealms](https://www.play.net/dr/) (aka DR). The idea is that it works with, rather than replacing your client. You probably heard of the [Lich Project](https://lichproject.org), this does something like that.

## Why the name?

Dobby is a house elf (creatures that are sworn to serve their master wizards). This proxy is supposed to do a similar function, help you.

## Why Java?

I decided to write dobby in Java for two reasons:

1. Java has pretty good cross-platform support; dobby should (one day) run on Windows, Mac OS, and Linux.
2. My personal preference, and hey, this is for me.

## How it works

Dobby is a telnet proxy that basically sits in-between your client and the DR server. This mean that dobby can modify your commands en-route to the server (aliasing) or send others on your behalf (scripting).

## How do I run it?

Getting started requires you to modify your /etc/hosts file to redirect DragonRealms traffic through your proxy. These steps should get simpler as development continues. One day all you may have to do is double-click dobby.jar and then open your client!

1. Add the following to your /etc/hosts file:

```bash
$ sudo vi /etc/hosts
```

```
127.0.0.1   dr.simutronics.net
```

2. Run a proxy for the DR game server.

```bash
$ java -jar dobby-{version}.jar 4901 199.188.208.5 4901
```

## How do I use it?

Once running, you interact with dobby by sending commands to the server. Dobby will intercept any command you send that starts with a semicolon (;).

### Scripting

Right now dobby supports StormFront (.sf) scripts that are in you Documents/dobby/ folder. The goal is to finish the full support of these scripts first. Dobby is not limited to one script at a time, but currently there's no way to stop a script once it starts (short of stopping dobby itself). Commands to list/stop/pause running scripts are in the works.

Dobby uses a CommandQueue to send all dobby-generated commands (user input is passed through to the server immediately). The command queue is a FIFO queue that automatically WAITs between commands and for roundtime. If a command fails because of type ahead or roundtime conditions, it generally will retry that command again. The goal is to make scripting simpler.

`;look.sf table` will run the [look.sf](https://github.com/ry4npw/dragonrealms-dobby/blob/master/src/test/resources/look.sf) script (assuming it's in the right place!). You'll end up seeing something like this:

```
>;look.sf table
dobby [look.sf: START]
dobby [look.sf: look in table]
There is nothing in there.
>
dobby [look.sf: look on table]
On the long stone table you see some flight glue, a band, a band,  and a glass of potato peel vodka.
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

* list/stop/pause currently running scripts

* support other scripting languages such as Genie, YASSE, Lich, and other interpreted languages such as JavaScript or Python.

* timed/scheduled commands
```
>;every 91 seconds PREDICT WEATHER
```

* support sending commands for other characters on the same computer (ask {toon} to {command})
```
>;ask weensie to TEACH PADHG FORGING
```

* add percentages to HEALTH/MANA. also add numerals to appraisals and combat messages.

* auto-mapping, with support for Genie map repository

* a RESTful interface to return real-time character information, this could make it easy to create a single-page HTML application to add things like health/spirit/stamina/fatigue bars to clients such as Avalon. Or by combining dobby services you could create a display of all your friends, their health/spirit/stamina/fatigue, location, and etc. User interfaces could also be done in the browser for things like script writing/management, or even for mapping programs.

* XML support

* upstream proxy support (for playing around firewalls)

