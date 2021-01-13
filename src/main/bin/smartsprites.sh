#!/bin/sh

#
# Add extra JVM options here
#
OPTS="-Xms64m -Xmx256m"

java $OPTS "-Djava.ext.dirs=` dirname "$0" | sed 's/ /\\ /g' `/lib" org.carrot2.labs.smartsprites.SmartSprites $@
