package com.soen691w;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Analyzer {
    private int numberOfClusters;

    private double maxClusterSize;
    private double maxStdDev;
    private double maxMean;


    private Cluster clusters[];


    public Analyzer(int numberOfClusters) {
        this.clusters = new Cluster[numberOfClusters];
        this.assignClusterData();
        this.getMaxValues();
        this.getSpikeData();
        this.getBloatData();
        this.getLeakData();

    }

    private void assignClusterData(){
        //do something with this.clusters
    }


    private void getMaxValues(){
        List<Double> avgMemoryVector =
                Arrays.stream(clusters)
                        .map(Cluster::getAverageMemoryDelta)
                        .collect(Collectors.toList());
        this.maxMean = Collections.max(avgMemoryVector);

        List<Double> avgStdDevVector =
                Arrays.stream(clusters)
                        .map(Cluster::getStandardDeviationMemoryDelta)
                        .collect(Collectors.toList());
        this.maxStdDev = Collections.max(avgStdDevVector);


        List<Double> clusterSizeVector =
                Arrays.stream(clusters)
                        .map(Cluster::getSize)
                        .collect(Collectors.toList());
        this.maxClusterSize = Collections.max(clusterSizeVector);


    }

    //spike of table v
    private void getSpikeData(){
        for(Cluster clusteri : this.clusters){
            float spikeForCluster;
            double numeratorSpike1 = clusteri.getSize() * clusteri.getStandardDeviationMemoryDelta();
            double denomiatorSpike1 = (this.maxClusterSize * this.maxStdDev);
            double sum1 = numeratorSpike1/denomiatorSpike1;
            double sum2 = clusteri.getAverageMemoryDelta()/this.maxMean;
            double spike = sum1 + sum2;
            clusteri.setSpike(spike);
        }
    }

    //bloat of table v
    private void getBloatData(){
        for(Cluster clusteri : this.clusters){
            double bloat = clusteri.getAverageMemoryDelta()/this.maxMean;
            clusteri.setBloat(bloat);
        }
    }

    private void getLeakData() {
        for (Cluster clusteri : this.clusters) {

            double sum1 = clusteri.getStandardDeviationMemoryDelta()/this.maxStdDev;
            double sum2 = clusteri.getAverageMemoryDelta()/this.maxMean;

            double leak = sum1 + sum2;
            clusteri.setLeak(leak);
        }
    }

    /*GETTERS AND SETTERS*/
    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public void setNumberOfClusters(int numberOfClusters) {
        this.numberOfClusters = numberOfClusters;
    }


    public Cluster[] getClusters() {
        return clusters;
    }

    public void setClusters(Cluster[] clusters) {
        this.clusters = clusters;
    }




}