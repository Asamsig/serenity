#!/bin/sh

sbt clean
sbt elastic-beanstalk:dist

eb deploy --staged --profile javabin

