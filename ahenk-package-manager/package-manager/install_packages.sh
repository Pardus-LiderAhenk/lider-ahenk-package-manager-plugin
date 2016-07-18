#!bin/bash

if [ $3 = "Kur" ] || [ $3 = "Install" ]; then
echo sudo apt-get --yes --force-yes install $1=$2
sudo apt-get --yes --force-yes install $1=$2
elif [ $3 = "Kaldır" ] || [ $3 = "Uninstall" ]; then
echo sudo apt-get --yes --force-yes purge $1=$2
sudo apt-get --yes --force-yes purge $1=$2
fi