package de.unijena.bioinf.ChemistryBase.chem;

import de.unijena.bioinf.ChemistryBase.ms.Ms2ExperimentAnnotation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InChI implements Ms2ExperimentAnnotation {

    public final String in3D;
    public final String in2D;
    public final String key;

    public InChI(String inchikey, String inchi) {
        if (inchi != null && inchi.endsWith("/")) inchi = inchi.substring(0, inchi.length()-1);
        this.in3D = inchi;
        this.key = inchikey;
        this.in2D = inchi==null ? null : inchi2d(inchi);
    }

    public MolecularFormula extractFormula() {
        int a=0;
        int b=0;
        for (a=0; a < in2D.length(); ++a) {
            if (in2D.charAt(a)=='/') break;
        }
        ++a;
        for (b=a; b < in2D.length(); ++b) {
            if (in2D.charAt(b)=='/') break;
        }

        MolecularFormula formula =  MolecularFormula.parse(in2D.substring(a, b));
        int q = getQCharge();
        if (q==0) return formula;
        else if (q<0){
            return formula.add(MolecularFormula.parse(String.valueOf(Math.abs(q)+"H")));
        } else {
            return formula.subtract(MolecularFormula.parse(String.valueOf(q+"H")));
        }
    }

    private static final Pattern Q_LAYER = Pattern.compile("\\/(q([^\\/]*))");
    /**
     * if structure is disconnected return charge of first connected component
     * @return
     */
    private int getQCharge() {
        Matcher matcher = Q_LAYER.matcher(in2D);
        if (matcher.find()){
            String charge = matcher.group(2).split(";")[0];
            if (charge.length()==0) return 0;
            return Integer.parseInt(charge);
        }
        return 0;
    }

    public String key2D() {
        return key.substring(0,14);
    }

    private static Pattern inchi2dPattern = Pattern.compile("/[btmrsfi]");
    public static String inchi2d(String inchi) {
        if (inchi.endsWith("/")) inchi = inchi.substring(0, inchi.length()-1);
        final Matcher m = inchi2dPattern.matcher(inchi);
        if (m.find()) {
            return inchi.substring(0, m.start());
        } else {
            return inchi;
        }
    }

    @Override
    public String toString(){
        if (in2D==null || key==null) {
            if (in2D!=null) return in2D;
            if (key==null) throw new NullPointerException();
            return key;
        } else {
            return key + " (" + in2D + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InChI inChI = (InChI) o;

        if (!in3D.equals(inChI.in3D)) return false;
        return !(key != null ? !key.equals(inChI.key) : inChI.key != null);

    }

    @Override
    public int hashCode() {
        int result = in3D.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
