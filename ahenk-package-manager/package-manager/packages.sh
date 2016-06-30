#!bin/bash

dpkg --get-selections | grep -v deinstall | dpkg-query --show
