#!bin/bash

dpkg --get-selections | grep -v deinstall > $1/package-manager/installed_packages.txt