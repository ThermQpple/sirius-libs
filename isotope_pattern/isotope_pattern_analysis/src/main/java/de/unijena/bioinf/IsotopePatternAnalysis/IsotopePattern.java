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
package de.unijena.bioinf.IsotopePatternAnalysis;

import de.unijena.bioinf.ChemistryBase.algorithm.scoring.Scored;
import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.ms.utils.SimpleSpectrum;
import de.unijena.bioinf.ms.annotations.TreeAnnotation;

public final class IsotopePattern extends Scored<MolecularFormula>  implements TreeAnnotation  {

    private final SimpleSpectrum pattern;

    public IsotopePattern(MolecularFormula candidate, double score, SimpleSpectrum pattern) {
        super(candidate, score);
        this.pattern = pattern;
    }

    public IsotopePattern withScore(double newScore) {
        return new IsotopePattern(getCandidate(),newScore,pattern);
    }

    public SimpleSpectrum getPattern() {
        return pattern;
    }

    public double getMonoisotopicMass() {
        return pattern.getMzAt(0);
    }

    @Override
    public String toString() {
        return getCandidate().toString() + "(" + getScore() + ") <- " + getPattern().toString();
    }
}
