#!/bin/bash

echo $1 | sudo tee --append /etc/apt/sources.list.d/ahenk.list > /dev/null
