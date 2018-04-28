#!/bin/python

import csv
import time
import datetime
import re

abstracted_log_file = 'baseline_abstracted_logs.txt'
perf_counter_file = 'baseline_perf_counters.csv'
time_slice_length = 2
time_stamp_format = '%H:%M:%S'
time_stamp_regex = '2[0-3]|[01][0-9]:[0-5][0-9]:[0-5][0-9]'
with open(abstracted_log_file) as abstractedLogFile:
	logEventReader = csv.reader(abstractedLogFile)
	firstLogLine = next(logEventReader)
	timeStampPattern = re.compile(time_stamp_regex)
	#initialize timeSlice
	if timeStampPattern.match(firstLogLine[0]):
		timeSliceStart = datetime.datetime.strptime(firstLogLine[0],time_stamp_format)
		timeSliceEnd = timeSliceStart + datetime.timedelta(seconds=time_slice_length)
		events = {}
		vectors = []
	#assign events to timeSlice
	for logLine in logEventReader:
		if timeStampPattern.match(logLine[0]):
			currentTimeStamp = datetime.datetime.strptime(logLine[0],time_stamp_format)
			#If currentTimeStamp is in the current timeSlice, count events
			if currentTimeStamp <= timeSliceEnd and currentTimeStamp >= timeSliceStart:
				events[logLine[1]] = events.setdefault(logLine[1],0) + 1
			#If currentTimeStamp is not in the current timeSlice, move to next timeSlice
			else:
				vectors.append([timeSliceEnd,events])
				timeSliceStart = timeSliceEnd
				timeSliceEnd = timeSliceStart + datetime.timedelta(seconds=time_slice_length)
				events = {}

#add memory deltas
perf_counter_file = 'baseline_perf_counters.csv'
time_stamp_format = '%H:%M:%S'
time_stamp_regex = '2[0-3]|[01][0-9]:[0-5][0-9]:[0-5][0-9]'
with open(perf_counter_file) as perfCounterFile:
	perfCounterReader = csv.reader(perfCounterFile)
	timeStampPattern = re.compile(time_stamp_regex)
	#initialize timeSlice
	vector_position = 0
	timeSliceStart = vectors[vector_position][0] - datetime.timedelta(seconds=time_slice_length)
	timeSliceEnd = vectors[vector_position][0]
	startCounter = 0
	endCounter = 0
	#get deltas for each timeSlice
	for line in perfCounterReader:
		#print("processing line "+str(line))
		#print("timeSlice "+str(timeSliceStart)+" , "+str(timeSliceEnd))
		if timeStampPattern.match(line[0]):
			currentTimeStamp = datetime.datetime.strptime(line[0],time_stamp_format)
			#If currentTimeStamp matches timeSliceStart, initialize start counter
			if currentTimeStamp == timeSliceStart:
				startCounter = int(line[2])
			#If currentTimeStamp matches timeSliceEnd,move to next timeSlice
			if currentTimeStamp == timeSliceEnd:
				endCounter = int(line[2])
				counterDelta = endCounter - startCounter
				vectors[vector_position].append(counterDelta)
				startCounter = endCounter
				timeSliceStart = timeSliceEnd
				timeSliceEnd = timeSliceStart + datetime.timedelta(seconds=time_slice_length)
				if vector_position < len(vectors)-1:
					vector_position = vector_position + 1
				else:
					#No more vectors left to add perf counters
					break
#Save time slice vectors
with open('baseline_time_slice_vectors.csv','wb') as vector_file:
	writer = csv.writer(vector_file)
	for row in vectors:
		writer.writerow(row)