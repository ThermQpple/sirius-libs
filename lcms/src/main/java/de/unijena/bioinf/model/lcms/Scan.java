package de.unijena.bioinf.model.lcms;

import de.unijena.bioinf.ChemistryBase.ms.CollisionEnergy;

/**
 * A spectrum which can be tracked back to a Scan within an LCMS source file
 */
public class Scan {

    /**
     * Unique scan number
     */
    private final int scanNumber;

    /**
     * retention time in milliseconds
     */
    private final long retentionTime;

    private final Polarity polarity;

    /**
     * For MS/MS only: precursor information
     */
    private final Precursor precursor;

    private final double TIC;
    private final int numberOfPeaks;
    private final double collisionEnergy;

    public Scan(int scanNumber, Polarity polarity, long retentionTime,double collisionEnergy, int numberOfPeaks, double TIC) {
        this(scanNumber,polarity,retentionTime,collisionEnergy,numberOfPeaks,TIC,null);
    }

    public Scan(int scanNumber, Polarity polarity, long retentionTime, double collisionEnergy,int numberOfPeaks,  double TIC, Precursor precursor) {
        this.scanNumber = scanNumber;
        this.retentionTime = retentionTime;
        this.collisionEnergy=collisionEnergy;
        this.precursor = precursor;
        this.polarity = polarity;
        this.TIC = TIC;
        this.numberOfPeaks = numberOfPeaks;
    }

    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    public double getTIC() {
        return TIC;
    }

    public int getScanNumber() {
        return scanNumber;
    }

    public boolean isMsMs() {
        return precursor!=null;
    }

    public Precursor getPrecursor() {
        return precursor;
    }

    public Polarity getPolarity() {
        return polarity;
    }

    public long getRetentionTime() {
        return retentionTime;
    }

    public double getCollisionEnergy(){return collisionEnergy;}

    @Override
    public String toString() {
        return precursor!=null ? ("MS/MS " + scanNumber + ", m/z = " + precursor.getMass()) : "MS " + scanNumber;
    }
}