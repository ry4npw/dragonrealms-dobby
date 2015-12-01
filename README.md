# dragonrealms-dobby

Dobby is a Java-based scripting proxy for [DragonRealms](https://www.play.net/dr/) (aka DR). The idea is that it works with, rather than replacing your client. You probably heard of the [Lich Project](https://lichproject.org), this does something like that.

## Why the name?

Dobby is a house elf (creatures that are sworn to serve their master wizards). This proxy is supposed to do a similar function, help you.

## How do I run it?

Getting started requires you to modify your /etc/hosts file to redirect DragonRealms traffic through your proxy. These steps should get simpler as development continues. One day all you may have to do is double-click dobby.jar and start your DR client!

1. Add the following to your /etc/hosts file:

```bash
$ sudo vi /etc/hosts
```

```
127.0.0.1   dr.simutronics.net
```

2. Run a proxy for the DR game server.

```bash
$ java -jar dobby-<version>.jar 4901 199.188.208.5 4901
```

## How do I use it?

Once running, you interact with dobby by sending commands to the server. Dobby will intercept your commands that start with a semicolon (;) and 

### Scripting

Right now dobby supports StormFront (.sf) scripts that are in you Documents/dobby/ folder.

```
;look.sf table
```

Will run the [look.sf](https://github.com/ry4npw/dragonrealms-dobby/blob/master/src/test/resources/look.sf) script (assuming it's in the right place!). You'll end up seeing something like this:

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
