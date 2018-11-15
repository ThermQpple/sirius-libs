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

import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ms.annotations.Annotated;
import de.unijena.bioinf.ms.annotations.Ms2ExperimentAnnotation;

import java.net.URL;
import java.util.List;

/**
 * A Ms2Experiment is a MS/MS measurement of a *single* compound. If there are multiple compounds measured in your
 * spectrum, clean up and separate them into multiple Ms2Experiment instances, too!
 */
public interface Ms2Experiment extends Cloneable, Annotated<Ms2ExperimentAnnotation> {


    URL getSource();

    String getName();

    /**
     * @return the ionization type of the ion (not null)
     */
    PrecursorIonType getPrecursorIonType();


    /**
     * Notes:
     * - If the data is preprocessed, then there should be a *clean* and single isotope pattern of the
     * ion in each ms1 spectrum. Further peaks are not allowed!
     *
     * @return a list of MS1 spectra with the isotope pattern of this compound
     */
    <T extends Spectrum<Peak>> List<T> getMs1Spectra();

    /**
     * In practice it seems more accurate to merge all MS1 spectra into a single one and only use this for further
     * analysis. Some tools provide a very accurate peak picking and merging, such that this is something which
     * should not be done in SIRIUS itself.
     *
     * @return merge all ms1 spectra to single one
     */
    <T extends Spectrum<Peak>> T getMergedMs1Spectrum();

    /**
     * @return a list of MS2 spectra belonging to this compound
     */
    <T extends Ms2Spectrum<Peak>> List<T> getMs2Spectra();

    /**
     * @return the mass-to-charge ratio of the ion to analyze
     */
    double getIonMass();

    /***
     * The further methods provide information which is OPTIONAL. The algorithm should be able to handle cases in
     * which this methods return NULL.
     */

    /**
     * The neutral mass is the mass of the molecule. In contrast the ion mass is the mass of the molecule + ion adduct.
     * Notice that the molecule may be also charged. That doesn't matter. Neutral says nothing about the charge, but
     * about the absence of an ionization.
     *
     * @return the *exact* (idealized) mass of the molecule or 0 if the mass is unknown
     */
    default double getMoleculeNeutralMass() {
        if (getMolecularFormula() != null)
            return getMolecularFormula().getMass();
        if (!getPrecursorIonType().isIonizationUnknown() && getIonMass() > 0)
            getPrecursorIonType().precursorMassToNeutralMass(getIonMass());
        return 0;
    }

    /**
     * @return molecular formula of the neutral molecule
     */
    MolecularFormula getMolecularFormula();

    /*
        Annotations are additional information, that might be present in the data or not. They are usually
        optional values and might be present in specific experiment types. You can use annotations instead of subclassing
        to extend the usable of the Ms2Experiment class.

        Annotations should be wrapped into a class, which is then also used as key
        For example: Instead of putting an annotation "Inchi" as string into this hashmap,
        you should create a class InChI that contains this string and then put this class into the hashmap
        Be aware that there is no support for subclasses. So annotation classes should be final
        Furthermore, annotations should be immutable. Prefer setting of annotations instead of changing the content
        of an annotation. The reason for this is that cloning an experiment will only do a shallow copy of the annotations
     */


    /**
     * Allow cloning/copying of Ms2Experiments
     * The implementation might choose if the cloning is deep or shallow
     */
    Ms2Experiment clone();


}
