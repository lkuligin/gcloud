#!/bin/bash

rm -f earthquakes.csv
wget http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_week.csv -O earthquakes.csv
