package de.unijena.bioinf.sirius.clustering;

public interface DistanceMeasureStrategy {


    public Distance calcNewDistance(Distance distance1, Distance distance2, int clusterSize1, int clusterSize2);

}