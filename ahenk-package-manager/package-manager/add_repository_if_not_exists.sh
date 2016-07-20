#!/bin/bash

if ! grep -q "$1" /etc/apt/sources.list /etc/apt/sources.list.d/*; then
    echo $1 | sudo tee --append /etc/apt/sources.list.d/ahenk.list > /dev/null
    sudo apt-get update
fi