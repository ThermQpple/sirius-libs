/*
 *
 *  This file is part of the SIRIUS library for analyzing MS and MS/MS data
 *
 *  Copyright (C) 2013-2020 Kai Dührkop, Markus Fleischauer, Marcus Ludwig, Martin A. Hoffman and Sebastian Böcker,
 *  Chair of Bioinformatics, Friedrich-Schilller University.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with SIRIUS. If not, see <https://www.gnu.org/licenses/lgpl-3.0.txt>
 */

package de.unijena.bioinf.lcms;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.BitSet;

class Extrema {

    protected final TDoubleArrayList extrema;
    protected final TIntArrayList indizes;

    public Extrema() {
        this.extrema = new TDoubleArrayList();
        this.indizes = new TIntArrayList();
    }

    public int getIndexAt(int k)  {
        return indizes.getQuick(k);
    }

    public boolean valid() {
        boolean minimum = false;
        for (int k=1; k < indizes.size(); ++k) {
            if (minimum) {
                if (extrema.getQuick(k) < extrema.getQuick(k-1)) return false;
            } else {
                if (extrema.getQuick(k) > extrema.getQuick(k-1)) return false;
            }
            minimum = !minimum;
        }
        return true;
    }

    public void addExtremum(int index, double intensity) {
        this.extrema.add(intensity);
        this.indizes.add(index);
    }

    void replaceLastExtremum(int index, double intensity) {
        extrema.set(extrema.size()-1, intensity);
        indizes.set(indizes.size()-1, index);
    }

    public double lastExtremumIntensity() {
        if (extrema.isEmpty()) return 0d;
        return extrema.getQuick(extrema.size()-1);
    }
    public double lastExtremum() {
        return indizes.getQuick(extrema.size()-1);
    }


    public int numberOfExtrema() {
        return extrema.size();
    }

    public boolean isMinimum(int k) {
        double v = extrema.getQuick(k);
        double w = extrema.getQuick(k > 0 ? k-1 : k+1);
        return v < w;
    }

    /**
     * removes extrema which differences are below noise level. However, if after removal less than
     * minimumNumberExtrema is kept, the removal is canceled.
     * @return true, if there are more than minimumNumberExtrema after smoothing
     */
    boolean smooth(float[] noiseLevelPerScan, int power, int minimumNumberExtrema) {
        final BitSet delete = new BitSet(indizes.size());
        for (int k=1; k < indizes.size()-1; ++k) {
            double intensityLeft = extrema.getQuick(k-1);
            double intensityRight = extrema.getQuick(k+1);
            double peak = extrema.getQuick(k);
            double avgSlope = (Math.abs(peak-intensityLeft)+Math.abs(peak-intensityRight))/2d;
            if (avgSlope <= noiseLevelPerScan[indizes.getQuick(k)]*power){
                delete.set(k);
            }
        }
        int cardinality = delete.cardinality();
        if (indizes.size()-cardinality >= minimumNumberExtrema && cardinality > 0) {
            final double[] newExtrema = new double[indizes.size()-cardinality];
            final int[] newIndizes = new int[indizes.size()-cardinality];
            boolean minimum = true;
            int k=-1;
            for (int j=0; j < indizes.size(); ++j) {
                if (!delete.get(j)) {
                    double newval = extrema.getQuick(j);
                    double prevVal = (k<0 ? 0 : newExtrema[k]);
                    if (minimum) {
                        if (newval <= prevVal) {
                            newExtrema[k] = newval;
                            newIndizes[k] = indizes.getQuick(j);
                        } else {
                            ++k;
                            newExtrema[k] = newval;
                            newIndizes[k] = indizes.getQuick(j);
                            minimum = false;
                        }
                    } else {
                        if (newval >= prevVal) {
                            newExtrema[k] = newval;
                            newIndizes[k] = indizes.getQuick(j);
                        } else {
                            ++k;
                            newExtrema[k] = newval;
                            newIndizes[k] = indizes.getQuick(j);
                            minimum = true;
                        }
                    }
                }
            }
            extrema.clearQuick();
            extrema.add(newExtrema, 0, k);
            indizes.clearQuick();
            indizes.add(newIndizes, 0, k);
            return true;
        } else return cardinality==0;
    }
}
