/*
 *
 *  This file is part of the SIRIUS library for analyzing MS and MS/MS data
 *
 *  Copyright (C) 2013-2020 Kai Dührkop, Markus Fleischauer, Marcus Ludwig, Martin A. Hoffman, Fleming Kretschmer and Sebastian Böcker,
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

package de.unijena.bioinf.FragmentationTreeConstruction.computation.graph;

import de.unijena.bioinf.ChemistryBase.ms.ft.FGraph;
import de.unijena.bioinf.ChemistryBase.ms.ft.Fragment;
import de.unijena.bioinf.sirius.ProcessedInput;

import java.util.List;

public interface LossValidator {

    public static class Combination implements LossValidator {

        protected final LossValidator[] validators;

        public Combination(List<LossValidator> validators) {
            this.validators = validators.toArray(new LossValidator[validators.size()]);
        }

        @Override
        public boolean isForbidden(ProcessedInput input, FGraph graph, Fragment a, Fragment b) {
            for (LossValidator v : validators) {
                if (v.isForbidden(input,graph,a,b))
                    return true;
            }
            return false;
        }
    }

    public boolean isForbidden(ProcessedInput input, FGraph graph, Fragment a, Fragment b);

}
