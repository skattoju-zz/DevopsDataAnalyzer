# DevopsDataAnalyzer
SOEN 691 W Winter 2018

# Structure

This repo consists of multiple programs than need to be run in sequence to perform the analysis described in the paper. There are two java programs that can built using gradle and two python programs. The python programs require the SciPy and Scikit Learn programs to be installed in order for them to work.

Steps in the analysis pipeline

# Log Abstraction

This done with a java program It takes as an input a path to a directory containing source code for hadoop as well as the log file to be abstracted

# Timeslice vector creation

This done with a python script called dataPrep.py that takes as an argument a log file and a perf counter file. It must be noted that the format should be as in the example files in the repo. The timestamps in log file and perf counter file should match in order to get results.

# Clustering

This is done with a python script called clustering.py that takes as an argument a file containing timeslice vectors and outputs clusters and corresponding memory deltas.

# Cluster Analysis

This part is done in the java program that takes an input clusters and corresponding memory deltas and outputs scores for each cluster.
