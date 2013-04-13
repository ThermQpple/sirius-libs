package de.unijena.bioinf.ChemistryBase.ms;

import java.util.Comparator;

public class CollisionEnergy {

    private final double minEnergy, maxEnergy;

    public CollisionEnergy(double min, double max) {
        if (min > max) throw new IllegalArgumentException("minimal energy have to be smaller than maximum energy");
        this.minEnergy = min;
        this.maxEnergy = max;
    }

    public boolean isOverlapping(CollisionEnergy other) {
        return minEnergy <= other.maxEnergy && maxEnergy >= other.minEnergy;
    }

    public double getMinEnergy() {
        return minEnergy;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public boolean lowerThan(CollisionEnergy o) {
        return maxEnergy < o.minEnergy;
    }
    public boolean greaterThan(CollisionEnergy o) {
        return minEnergy > o.maxEnergy;
    }

    public CollisionEnergy merge(CollisionEnergy other) {
        return new CollisionEnergy(Math.min(minEnergy, other.minEnergy), Math.max(maxEnergy, other.maxEnergy));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CollisionEnergy) return equals((CollisionEnergy)obj);
        return false;
    }

    public boolean equals(CollisionEnergy obj) {
        if (this == obj) return true;
        return Math.abs(minEnergy-obj.minEnergy) < 1e-12 && Math.abs(maxEnergy-obj.maxEnergy) < 1e-12;
    }

    @Override
    public String toString() {
        if (minEnergy == maxEnergy) return stringify(minEnergy);
        return stringify(minEnergy) + " - " + stringify(maxEnergy);
    }

    private static String stringify(double minEnergy) {
        if (Math.abs((int) minEnergy - minEnergy) < 1e-12) return String.valueOf((int)minEnergy);
        return String.valueOf(minEnergy);
    }

    public static Comparator<CollisionEnergy> getMinEnergyComparator() {
        return new Comparator<CollisionEnergy>() {
            @Override
            public int compare(CollisionEnergy o1, CollisionEnergy o2) {
                return Double.compare(o1.minEnergy, o2.minEnergy);
            }
        };
    }

}
