FROM ubuntu
MAINTAINER Minh Cuong Tran

RUN apt-get update
RUN apt-get -y install git
RUN apt-get -y install default-jdk
RUN apt-get -y install ant
RUN apt-get -y install javacc

RUN git clone https://github.com/tranmh/AdaLogo.git
RUN mkdir AdaLogo/bin
RUN ant release -f AdaLogo/build.xml
RUN ls -la AdaLogo/bin
