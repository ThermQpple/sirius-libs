package de.unijena.bioinf.FragmentationTreeConstruction.computation.scoring;

import de.unijena.bioinf.ChemistryBase.algorithm.ParameterHelper;
import de.unijena.bioinf.ChemistryBase.chem.Ionization;
import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.data.DataDocument;
import de.unijena.bioinf.ChemistryBase.ms.PossibleIonModes;
import de.unijena.bioinf.sirius.ProcessedInput;
import de.unijena.bioinf.sirius.ProcessedPeak;

public class AdductTypeScorer implements DecompositionScorer<PossibleIonModes>{
    @Override
    public PossibleIonModes prepare(ProcessedInput input) {
        return input.getAnnotation(PossibleIonModes.class,null);
    }

    @Override
    public double score(MolecularFormula formula, Ionization ion, ProcessedPeak peak, ProcessedInput input, PossibleIonModes precomputed) {
        if (precomputed==null) return 0d;
        else return Math.log(precomputed.getProbabilityFor(ion));
    }

    @Override
    public <G, D, L> void importParameters(ParameterHelper helper, DataDocument<G, D, L> document, D dictionary) {

    }

    @Override
    public <G, D, L> void exportParameters(ParameterHelper helper, DataDocument<G, D, L> document, D dictionary) {

    }
}
