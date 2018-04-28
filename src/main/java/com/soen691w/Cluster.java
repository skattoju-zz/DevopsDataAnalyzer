package com.soen691w;


public class Cluster {

    private int clusterNumber;
    private double spike;
    private double bloat;
    private double leak;
    private double [] memoryDeltas;


    //avg memory delta of all time slice profiles in this cluster
    private double averageMemoryDelta;

    //standard deviation of memory delta of all mem deltas of this cluster
    private double standardDeviationMemoryDelta;

    //size of cluster
    private double size;

    public Cluster(int clusterNumber, int clusterSize, double [] memoryDeltas){
        this.clusterNumber = clusterNumber;
        this.size = clusterSize;
        this.memoryDeltas = memoryDeltas;
        this.initAverage();
        this.initStdDev();
    }

    /**
     * Initialize the avg for the cluster.
     */
    private void initAverage(){
        double average = 0.0;
        for(int i = 0;i < this.memoryDeltas.length;i++){
            average += this.memoryDeltas[i];
        }
        average = average/this.memoryDeltas.length;
        this.averageMemoryDelta = average;
    }

    /**
     * Initilize the standard deviation for the cluster.
     * @return
     */
    private void initStdDev() {
        this.standardDeviationMemoryDelta = Math.sqrt(getVariance());
    }

    private double getVariance() {
        double temp = 0;
        for(double a :this.memoryDeltas)
            temp += (a-this.averageMemoryDelta)*(a-this.averageMemoryDelta);
        return temp/(size);
    }

    public double getSpike() {
        return spike;
    }

    public void setSpike(double spike) {
        this.spike = spike;
    }

    public double getBloat() {
        return bloat;
    }

    public void setBloat(double bloat) {
        this.bloat = bloat;
    }

    public double getLeak() {
        return leak;
    }

    public void setLeak(double leak) {
        this.leak = leak;
    }


    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getAverageMemoryDelta() {
        return averageMemoryDelta;
    }

    public void setAverageMemoryDelta(double averageMemoryDelta) {
        this.averageMemoryDelta = averageMemoryDelta;
    }

    public double getStandardDeviationMemoryDelta() {
        return standardDeviationMemoryDelta;
    }

    public void setStandardDeviationMemoryDelta(double standardDeviationMemoryDelta) {
        this.standardDeviationMemoryDelta = standardDeviationMemoryDelta;
    }

    public double[] getMemoryDeltas() {
        return memoryDeltas;
    }

    public void setMemoryDeltas(double[] memoryDeltas) {
        this.memoryDeltas = memoryDeltas;
    }

    public int getClusterNumber(){
        return this.clusterNumber;
    }


}