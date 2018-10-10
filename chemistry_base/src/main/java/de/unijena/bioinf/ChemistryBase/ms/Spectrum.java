/*
 *  This file is part of the SIRIUS library for analyzing MS and MS/MS data
 *
 *  Copyright (C) 2013-2015 Kai Dührkop
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with SIRIUS.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.unijena.bioinf.ChemistryBase.ms;


public interface Spectrum<T extends Peak> extends Iterable<T>, Cloneable {

    double getMzAt(int index);

    double getIntensityAt(int index);

    T getPeakAt(int index);

    int size();


    /*
     * This are the only extensions we need to be Compatible with myxo viewers and stuff, i know this is not the perfect model
     * but it is painless and costs no memory for non MSn data.
     */

    /**
     * @return the collision energy (type) of the fragmentation cell. If no MSn data it returns CollisionEnergy.none()
     */
    default CollisionEnergy getCollisionEnergy() {
        return CollisionEnergy.none();
    }

    /**
     * The MS level. use 1 for MS1 and 2 for MS2 spectra.
     *
     * @return MS of the spectrum
     */
    default int getMsLevel() {
        return 1;
    }

    /**
     * @return The highest intensity of all peaks in the spectrum
     */
    default double getMaxIntensity() {
        final int s = size();
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < s; i++) {
            max = Math.max(getIntensityAt(i), max);
        }
        return max;
    }
}
