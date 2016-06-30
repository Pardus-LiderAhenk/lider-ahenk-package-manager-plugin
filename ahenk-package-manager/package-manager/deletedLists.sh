#!/bin/bash

IFS='
'
for filename in `sudo grep -rn '/etc/apt/' -e $1`; do echo $filename;
done