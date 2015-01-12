# AdaLogo
Learning Programming Language Ada with the Turtle

## Webpage
The [webpage in German](http://tranmh.github.io/AdaLogo/) has a download area for the adalogo.jar file. The UserGuide section describes how to use AdaLogo. Please read the Reference to learn the AdaLogo programming language. Thanks to Dr. Stefan Lewandowski there is course material from the lecture Informatik I. 

## Run
To run AdaLogo the simplest way is to download adalogo.jar and execute in Linux:
```
java -jar adalogo.jar
```
In Windows just double-click on adalogo.jar file after installing Java.

## Build
The build system is ant with different targets. You need javacc to compile AdaLogo. The following commands work on an Ubuntu 14.10 machine:
```
sudo apt-get install ant
sudo apt-get install javacc
git clone https://github.com/tranmh/AdaLogo.git
cd AdaLogo
mkdir bin
ant release
# to run:
java -jar release/adalogo.jar
```