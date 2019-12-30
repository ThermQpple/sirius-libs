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
package de.unijena.bioinf.FragmentationTreeConstruction.computation.scoring;

import de.unijena.bioinf.ChemistryBase.algorithm.Called;
import de.unijena.bioinf.ChemistryBase.algorithm.ParameterHelper;
import de.unijena.bioinf.ChemistryBase.data.DataDocument;
import de.unijena.bioinf.ChemistryBase.math.ByMedianEstimatable;
import de.unijena.bioinf.ChemistryBase.math.ParetoDistribution;
import de.unijena.bioinf.ChemistryBase.math.RealDistribution;
import de.unijena.bioinf.ChemistryBase.ms.MedianNoiseIntensity;
import de.unijena.bioinf.sirius.ProcessedInput;
import de.unijena.bioinf.sirius.ProcessedPeak;

import java.util.List;

@Called("Intensity")
public class PeakIsNoiseScorer implements PeakScorer {

    private ByMedianEstimatable<? extends RealDistribution> distribution;

    public ByMedianEstimatable<? extends RealDistribution> getDistribution() {
        return distribution;
    }

    public void setDistribution(ByMedianEstimatable<? extends RealDistribution> distribution) {
        this.distribution = distribution;
    }

    public PeakIsNoiseScorer() {
        this(ParetoDistribution.getMedianEstimator(0.005));
    }

    public PeakIsNoiseScorer(ByMedianEstimatable<? extends RealDistribution> distribution) {
        this.distribution = distribution;
    }

    @Override
    public void score(List<ProcessedPeak> peaks, ProcessedInput input, double[] scores) {
        final RealDistribution estimatedDistribution = distribution.extimateByMedian(
                input.getExperimentInformation().getAnnotationOrDefault(MedianNoiseIntensity.class).value);
        for (int i=0; i < peaks.size(); ++i) {
            scores[i] -= estimatedDistribution.getInverseLogCumulativeProbability(peaks.get(i).getRelativeIntensity());
        }
    }

    @Override
    public <G, D, L> void importParameters(ParameterHelper helper, DataDocument<G, D, L> document, D dictionary) {
        this.distribution = (ByMedianEstimatable<RealDistribution>)helper.unwrap(document, document.getFromDictionary(dictionary, "distribution"));
    }

    @Override
    public <G, D, L> void exportParameters(ParameterHelper helper, DataDocument<G, D, L> document, D dictionary) {
        document.addToDictionary(dictionary, "distribution", helper.wrap(document,distribution));
    }
}
