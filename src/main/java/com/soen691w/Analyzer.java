package com.soen691w;


import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.util.ArrayList;
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
    private ArrayList<Cluster> clusterList = new ArrayList<Cluster>();

    public void  processMemoryDeltas(){
        this.readDeltasFromFile();
        this.getMaxValues();
        this.getSpikeData();
        this.getBloatData();
        this.getLeakData();
    }

    private void readDeltasFromFile(){
        try (BufferedReader br = new BufferedReader(new FileReader(Main.memoryDeltaFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                assignClusterData(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        clusters = clusterList.toArray(new Cluster[clusterList.size()]);
    }


    private void assignClusterData(String line){
        try{
            String idString = line.substring(0, line.indexOf(","));
            int id = Integer.parseInt(idString);
            String memoryDeltas = line.substring(line.indexOf(",")+1);
            memoryDeltas = memoryDeltas.replace("[", "");
            memoryDeltas = memoryDeltas.replace("]", "");
            memoryDeltas = memoryDeltas.replace("\"", "");
            //String[] memoryDeltaStringArray = memoryDeltas.split(",");
            double[] memoryDeltaArray = Arrays.stream(memoryDeltas.split(",\\s+")).mapToDouble(Double::parseDouble).toArray();
            Cluster cluster = new Cluster(id, memoryDeltaArray.length, memoryDeltaArray);
            clusterList.add(cluster);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
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

            double sum1 = clusteri.
                    getStandardDeviationMemoryDelta()/this.maxStdDev;
            double sum2 = clusteri.getAverageMemoryDelta()/this.maxMean;

            double leak = sum1 + sum2;
            clusteri.setLeak(leak);
        }
    }

    public void printOutlyingClusters(){
        List<Double> bloats =
                Arrays.stream(clusters)
                        .map(Cluster::getBloat)
                        .collect(Collectors.toList());
        double avgBloat = bloats.stream().mapToDouble(val->val).average().getAsDouble();


        List<Double> leaks =
                Arrays.stream(clusters)
                        .map(Cluster::getLeak)
                        .collect(Collectors.toList());
        double avgLeak = leaks.stream().mapToDouble(val->val).average().getAsDouble();


        List<Double> spikes =
                Arrays.stream(clusters)
                        .map(Cluster::getSpike)
                        .collect(Collectors.toList());
        double avgSpike = spikes.stream().mapToDouble(val->val).average().getAsDouble();


        for (Cluster c : clusters)
            if(c.getBloat() > 25*avgBloat)
                System.out.println("Bloat: " + c.getClusterNumber() + " Score: " + c.getBloat());
        System.out.println();

        for (Cluster c : clusters)
            if(c.getLeak() > 25*avgLeak )
                System.out.println("Leak: " + c.getClusterNumber()  + " Score: " + c.getLeak());
        System.out.println();

        for (Cluster c : clusters)
            if(c.getSpike() > 25*avgSpike)
                System.out.println("Spike " + c.getClusterNumber() + " Score: " + c.getSpike());


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