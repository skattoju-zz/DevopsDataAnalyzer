#!/bin/python
import csv
from matplotlib import pyplot as plt
from scipy.cluster.hierarchy import dendrogram, linkage, to_tree, cut_tree, fcluster
from scipy.spatial.distance import pdist
from scipy.stats import pearsonr
from sklearn.metrics import calinski_harabaz_score
import collections
import numpy as np

total_events = 130
event_vector = np.zeros(total_events)
event_vector_list = []
timeStampList = []
eventDictList = []
memoryDeltas = []
time_slices = 'bloat_time_slice_vectors10000x.csv'
cluster_memory_deltas = 'bloat_cluster_memory_deltas10000x.csv'
with open(time_slices) as timeSlices:
	timeSliceReader = csv.reader(timeSlices)
	for timeSlice in timeSliceReader:
		#eval is dangerous / insecure
		timeStampList.append(timeSlice[0])
		event_dict = eval(timeSlice[1])
		memoryDeltas.append(int(timeSlice[2]))
		eventDictList.append(event_dict)
		for event in event_dict:
			event_vector[int(event[1:])] = event_dict[event]
		event_vector_list.append(event_vector)
		event_vector = np.zeros(total_events)
event_array = np.asarray(event_vector_list)

n = len(event_array)
def mydist(p1, p2):
	pearson = pearsonr(p1,p2)
	modified_pearson = ((n*np.sum(p1*p2))-(np.sum(p1)*np.sum(p2)))/((np.sqrt(n*np.sum(np.square(p1))-(np.square(np.sum(p1))*(np.sum(np.square(p2))-np.square(np.sum(p2)))))))
	#print(modified_pearson)
	#print(pearson)
	metric = pearson[0]
	#metric = modified_pearson
	if metric > 0:
		#result = 9.32798134406 - metric
		result = 1 - metric
	else:
		result = abs(metric)
	return result


Z = linkage(event_array,'average',mydist, True)

def fancy_dendrogram(*args, **kwargs):
    max_d = kwargs.pop('max_d', None)
    if max_d and 'color_threshold' not in kwargs:
        kwargs['color_threshold'] = max_d
    annotate_above = kwargs.pop('annotate_above', 0)

    ddata = dendrogram(*args, **kwargs)

    if not kwargs.get('no_plot', False):
        plt.title('Hierarchical Clustering Dendrogram')
        plt.xlabel('sample index or (cluster size)')
        plt.ylabel('distance')
        for i, d, c in zip(ddata['icoord'], ddata['dcoord'], ddata['color_list']):
            x = 0.5 * sum(i[1:3])
            y = d[1]
            if y > annotate_above:
                plt.plot(x, y, 'o', c=c)
                plt.annotate("%.3g" % y, (x, y), xytext=(0, -5),
                             textcoords='offset points',
                             va='top', ha='center')
        if max_d:
            plt.axhline(y=max_d, c='k')
    return ddata


fancy_dendrogram(
    Z,
    truncate_mode='none',
    p=12,
    leaf_rotation=90.,
    leaf_font_size=12.,
    show_contracted=True,
    annotate_above=2,
    max_d=0.04,
)

# Plot dendrogram
plt.show()
#print(Z)

# Print CH Scores
#for d in range(10):
	# Sometimes there are too many clusters ?
	# print("no of events "+str(len(event_array)))
	# print("no of labels "+str(len(fcluster(Z,d/10,'distance'))))
#	print("CH score "+str(calinski_harabaz_score(event_array,fcluster(Z,d/100,'distance'))))

clusterList = fcluster(Z,0.04,'distance')
clusterMemoryDeltas = {}
clusterEvents = {}
for i in range(len(clusterList)):
	clusterMemoryDeltas[clusterList[i]] = clusterMemoryDeltas.setdefault(clusterList[i],[])
	clusterEvents[clusterList[i]] = clusterEvents.setdefault(clusterList[i],[])
	clusterMemoryDeltas[clusterList[i]].append(memoryDeltas[i])
	clusterEvents[clusterList[i]].append(eventDictList[i])

with open(cluster_memory_deltas,'wb') as clusteringOutput:
	clusteringOutputWriter = csv.writer(clusteringOutput)
	for cluster in clusterMemoryDeltas:
		# write clusters for manual inspection
		# clusteringOutputWriter.writerow([cluster,clusterEvents[cluster],clusterMemoryDeltas[cluster]])
		# write clusters for analysis program
		clusteringOutputWriter.writerow([cluster,clusterMemoryDeltas[cluster]])
