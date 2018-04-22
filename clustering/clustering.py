#!/bin/python
import csv
from matplotlib import pyplot as plt
from scipy.cluster.hierarchy import dendrogram, linkage, to_tree, cut_tree, fcluster
from scipy.spatial.distance import pdist
from scipy.stats import pearsonr
import numpy as np

total_events = 130
event_vector = np.zeros(total_events)
event_vector_list = []
memoryDeltas = []
time_slices = 'time_slice_vectors.csv'
cluster_memory_deltas = 'cluster_memory_deltas.csv'
with open(time_slices) as timeSlices:
	timeSliceReader = csv.reader(timeSlices)
	for timeSlice in timeSliceReader:
		#eval is dangerous / insecure
		event_dict = eval(timeSlice[1])
		memoryDeltas.append(int(timeSlice[2]))
		for event in event_dict:
			event_vector[int(event[1:])-1] = event_dict[event]
		event_vector_list.append(event_vector)
		event_vector = np.zeros(total_events)
event_array = np.asarray(event_vector_list)
Z = linkage(event_array,'single','euclidean', True)

def fancy_dendrogram(*args, **kwargs):
    max_d = kwargs.pop('max_d', None)
    if max_d and 'color_threshold' not in kwargs:
        kwargs['color_threshold'] = max_d
    annotate_above = kwargs.pop('annotate_above', 0)

    ddata = dendrogram(*args, **kwargs)

    if not kwargs.get('no_plot', False):
        plt.title('Hierarchical Clustering Dendrogram (truncated)')
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
    max_d=6,
)
plt.show()
clusterList = fcluster(Z,6,'distance')
clusterMemoryDeltas = {}
for i in range(len(clusterList)):
	clusterMemoryDeltas[clusterList[i]] = clusterMemoryDeltas.setdefault(clusterList[i],[])
	clusterMemoryDeltas[clusterList[i]].append(memoryDeltas[i])
#print(cluster_memory_deltas)
with open(cluster_memory_deltas,'wb') as clusteringOutput:
	clusteringOutputWriter = csv.writer(clusteringOutput)
	for cluster in clusterMemoryDeltas:
		clusteringOutputWriter.writerow([cluster,clusterMemoryDeltas[cluster]])
