package de.unijena.bioinf.FragmentationTreeConstruction.computation.recalibration;

import de.unijena.bioinf.ChemistryBase.ms.MutableMs2Spectrum;
import de.unijena.bioinf.ChemistryBase.ms.ft.RecalibrationFunction;
import de.unijena.bioinf.FragmentationTreeConstruction.model.MS2Peak;
import de.unijena.bioinf.FragmentationTreeConstruction.model.ProcessedPeak;
import de.unijena.bioinf.ms.annotations.Annotaion;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Identity;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

public class SpectralRecalibration implements Annotaion {

    private final static SpectralRecalibration NONE = new SpectralRecalibration(null,null,null);

    public static SpectralRecalibration none() {
        return NONE;
    }

    protected final UnivariateFunction[] recalibrationFunctions;
    protected final UnivariateFunction mergedFunc;
    protected final MutableMs2Spectrum[] originalSpectra;

    public SpectralRecalibration(MutableMs2Spectrum[] originalSpectra, UnivariateFunction[] recalibrationFunctions, UnivariateFunction mergedFunc) {
        this.recalibrationFunctions = recalibrationFunctions;
        this.mergedFunc = mergedFunc==null ? new Identity() : mergedFunc;
        this.originalSpectra = originalSpectra;
    }

    public UnivariateFunction getRecalibrationFunction() {
        return mergedFunc;
    }

    public UnivariateFunction getRecalibrationFunctionFor(MutableMs2Spectrum spec) {
        if (recalibrationFunctions==null) return mergedFunc;
        final UnivariateFunction f = recalibrationFunctions[spec.getScanNumber()];
        if (f==null) return mergedFunc;
        else return f;
    }

    public double recalibrate(ProcessedPeak peak) {
        if (this==NONE) return peak.getOriginalMz();
        // 1. check if most intensive original peak can be recalibrated
        MS2Peak mostIntensive = null;
        for (MS2Peak m : peak.getOriginalPeaks()) {
            if (mostIntensive==null || m.getIntensity() > mostIntensive.getIntensity())
                mostIntensive = m;
        }
        if (mostIntensive!=null) {
            final int sc = ((MutableMs2Spectrum)mostIntensive.getSpectrum()).getScanNumber();
            if (recalibrationFunctions[sc]!=null) {
                return recalibrationFunctions[sc].value(peak.getOriginalMz());
            }
        }
        // 2. use merged recalibration function
        return mergedFunc.value(peak.getOriginalMz());
    }


    public RecalibrationFunction toPolynomial() {
        final UnivariateFunction f = getRecalibrationFunction();
        if (f instanceof PolynomialFunction)
            return new RecalibrationFunction(((PolynomialFunction) f).getCoefficients());
        else if (f instanceof Identity)
            return RecalibrationFunction.identity();
        else throw new RuntimeException("Cannot represent " + f + " as polynomial function");
    }
}
