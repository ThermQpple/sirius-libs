package de.unijena.bioinf.ChemistryBase.ms;

import com.google.common.base.Joiner;
import de.unijena.bioinf.ms.annotations.Ms2ExperimentAnnotation;
import de.unijena.bioinf.ms.properties.DefaultInstanceProvider;
import de.unijena.bioinf.ms.properties.DefaultProperty;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Keywords that can be assigned to a input spectrum to judge its quality. Available keywords are: Good, LowIntensity, NoMS1Peak, FewPeaks, Chimeric, NotMonoisotopicPeak, PoorlyExplained
 */
public class CompoundQuality implements Ms2ExperimentAnnotation  {

    private final EnumSet<CompoundQualityFlag> flags;

    public static CompoundQuality fromString(String s) {
        return fromKeywords(Arrays.asList(s.split("\\s*,\\s*")));
    }

    @DefaultInstanceProvider
    public static CompoundQuality fromKeywords(@DefaultProperty List<String> value) {
        final EnumSet<CompoundQualityFlag> props = EnumSet.noneOf(CompoundQualityFlag.class);
        for (String property : value) {
            try {
                props.add(CompoundQualityFlag.valueOf(property));
            } catch (IllegalArgumentException e) {
                LoggerFactory.getLogger(CompoundQuality.class).error("Unknown spectrum property with name '" + property + "'");
            }
        }
        return new CompoundQuality(props);
    }

    public CompoundQuality() {
        this.flags = EnumSet.of(CompoundQualityFlag.UNKNOWN);
    }

    public CompoundQuality(CompoundQualityFlag first, CompoundQualityFlag... other) {
        this.flags = EnumSet.of(first, other);
        validateInput(flags);
    }

    private CompoundQuality(EnumSet<CompoundQualityFlag> flags) {
        this.flags = flags;
        validateInput(flags);
    }

    private void validateInput(EnumSet<CompoundQualityFlag> flags){
        if (flags.size()>1){
            if (flags.contains(CompoundQualityFlag.Good)){
                throw new IllegalArgumentException("Compound quality flag 'Good' can only assigned to compounds without adding additional quality flags.");
            } else if (flags.contains(CompoundQualityFlag.UNKNOWN)){
                throw new IllegalArgumentException("Compound quality flag 'UNKNOWN' can only assigned to compounds without adding additional quality flags.");
            }
        }
    }

    public boolean is(CompoundQualityFlag flag) {
        return flags.contains(flag);
    }

    public boolean isNot(CompoundQualityFlag flag) {
        return !flags.contains(flag);
    }

    public static enum CompoundQualityFlag {
        Good, LowIntensity, NoMS1Peak, FewPeaks, Chimeric, NotMonoisotopicPeak, PoorlyExplained, UNKNOWN,
        BadIsotopePattern, BadPeakShape;
    }

    public String toString() {
        return Joiner.on(',').join(flags);
    }

    /**
     *
     * @return true if only Good flag is contained or nothing is known about quality.
     */
    public boolean isNotBadQuality(){
        return flags.size()==0 || flags.contains(CompoundQualityFlag.Good) || (flags.size()==1 && (flags.contains(CompoundQualityFlag.UNKNOWN)));
    }

    public CompoundQuality updateQuality(CompoundQualityFlag flag){
        if (flag==CompoundQualityFlag.Good){
            //quality Good overrides the rest
            return new CompoundQuality(CompoundQualityFlag.Good);
        } else if (flag==CompoundQualityFlag.UNKNOWN){
            //todo quality UNKNOWN overrides the rest as well?
            return new CompoundQuality(CompoundQualityFlag.UNKNOWN);
        } else {
            EnumSet<CompoundQualityFlag> updatedFlags = flags.clone();
            //remove unspecific flags
            updatedFlags.remove(CompoundQualityFlag.Good);
            updatedFlags.remove(CompoundQualityFlag.UNKNOWN);

            updatedFlags.add(flag);

            return new CompoundQuality(updatedFlags);
        }
    }

}
