
package de.unijena.bioinf.FragmentationTreeConstruction.computation.scoring;

import de.unijena.bioinf.ChemistryBase.algorithm.Called;
import de.unijena.bioinf.ChemistryBase.algorithm.ParameterHelper;
import de.unijena.bioinf.ChemistryBase.chem.Ionization;
import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.chem.utils.MolecularFormulaScorer;
import de.unijena.bioinf.ChemistryBase.chem.utils.scoring.ChemicalCompoundScorer;
import de.unijena.bioinf.ChemistryBase.data.DataDocument;
import de.unijena.bioinf.sirius.ProcessedInput;
import de.unijena.bioinf.sirius.ProcessedPeak;

@Called("Chemical Prior")
public class ChemicalPriorScorer implements DecompositionScorer<Object> {

    public static final double LEARNED_NORMALIZATION_CONSTANT = 0.17546357436139415d;
    public static final double LEARNED_NORMALIZATION_CONSTANT_FOR_ROOT = 0.43916395724493595d;
    private MolecularFormulaScorer prior;
    private double normalizationConstant, minimalMass;

    public ChemicalPriorScorer() {
        this(ChemicalCompoundScorer.createDefaultCompoundScorer(), LEARNED_NORMALIZATION_CONSTANT, 100d);
    }

    public ChemicalPriorScorer(MolecularFormulaScorer prior, double normalizationConstant) {
        this(prior, normalizationConstant, 100d);
    }

    public ChemicalPriorScorer(MolecularFormulaScorer prior, double normalizationConstant, double minimalMass) {
        assert minimalMass >= 0 && normalizationConstant < 10; // just to be shure that nobody mix both parameters ^^°
        this.prior = prior;
        this.normalizationConstant = normalizationConstant;
        this.minimalMass = minimalMass;
    }

    public MolecularFormulaScorer getPrior() {
        return prior;
    }

    public void setPrior(MolecularFormulaScorer prior) {
        this.prior = prior;
    }

    public double getNormalizationConstant() {
        return normalizationConstant;
    }

    public void setNormalizationConstant(double normalizationConstant) {
        this.normalizationConstant = normalizationConstant;
    }

    public double getMinimalMass() {
        return minimalMass;
    }

    public void setMinimalMass(double minimalMass) {
        this.minimalMass = minimalMass;
    }

    @Override
    public Object prepare(ProcessedInput input) {
        return null;
    }

    public double score(MolecularFormula formula) {
        return formula.getMass() >= minimalMass ? Math.max(-10d, prior.score(formula)) - normalizationConstant : 0d;
    }

    @Override
    public double score(MolecularFormula formula, Ionization ion, ProcessedPeak peak, ProcessedInput input, Object precomputed) {
        return formula.getMass() >= minimalMass ? Math.max(-10d, prior.score(formula)) - normalizationConstant : 0d;
    }

    @Override
    public <G, D, L> void importParameters(ParameterHelper helper, DataDocument<G, D, L> document, D dictionary) {
        this.prior = (MolecularFormulaScorer)helper.unwrap(document, document.getFromDictionary(dictionary, "prior"));
        this.normalizationConstant = document.getDoubleFromDictionary(dictionary, "normalization");
        this.minimalMass = document.getDoubleFromDictionary(dictionary, "minimalMass");
    }

    @Override
    public <G, D, L> void exportParameters(ParameterHelper helper, DataDocument<G, D, L> document, D dictionary) {
        document.addToDictionary(dictionary, "prior", helper.wrap(document, prior));
        document.addToDictionary(dictionary, "normalization", normalizationConstant);
        document.addToDictionary(dictionary, "minimalMass", minimalMass);
    }
}
