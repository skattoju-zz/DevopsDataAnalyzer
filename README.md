# DevopsDataAnalyzer
SOEN 691 W Winter 2018

# Structure

This repo consists of multiple programs than need to be run in sequence to perform the analysis described in the paper. There is a main java program that can built using gradle and two python programs. The python programs require the SciPy and Scikit Learn libraries to be installed in order for them to work.

# Steps in the analysis pipeline

The main java program executes the following steps. Be warned that this is still a prototype and many parameters are hard coded.

## Log Abstraction

This done with a java program It takes as an input a path to a directory containing source code for hadoop as well as the log file to be abstracted. It outputs a file with timestamps and event ids.

## Timeslice vector creation

This done with a python script called dataprep.py that takes as an argument an abstracted log file and a perf counter file. It must be noted that the format should be as in the example files in the repo. The timestamps in log file and perf counter file should match in order to get results. The script outputs timeslice vectors as described in the paper.

## Clustering

This is done with a python script called clustering.py that takes as an argument a file containing timeslice vectors and outputs clusters and corresponding memory deltas.

## Cluster Analysis


This part is done in the java program. It takes an input clusters and corresponding memory deltas and outputs scores for each cluster.
