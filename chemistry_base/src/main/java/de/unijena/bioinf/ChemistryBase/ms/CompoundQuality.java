package de.unijena.bioinf.ChemistryBase.ms;

import java.util.*;

/**
 * Created by ge28quv on 04/07/17.
 */
public class CompoundQuality {
    List<SpectrumProperty> properties;

    public CompoundQuality() {
        this(null);
    }

    public CompoundQuality(SpectrumProperty property) {
        properties = new ArrayList<>();
        if (property!=null) properties.add(property);
    }

    public void addProperty(SpectrumProperty property) {
        if (properties.contains(property)) return;
        properties.add(property);
    }

    public List<SpectrumProperty> getProperties() {
        return properties;
    }

    public boolean isGoodQuality(){
        //todo what is standard?
        for (SpectrumProperty property : properties) {
            if (property.equals(SpectrumProperty.Good)) return true;
        }
        return false;
    }

    public boolean isNotBadQuality() {
        for (SpectrumProperty property : properties) {
            if (!property.equals(SpectrumProperty.Good)) return false;
        }
        return true;
    }

    public boolean hasProperty(SpectrumProperty property) {
        for (SpectrumProperty p : properties) {
            if (property.equals(p)) return true;
        }
        return false;
    }

    public boolean removeProperty(SpectrumProperty property) {
        Iterator<SpectrumProperty> iterator = properties.iterator();
        while (iterator.hasNext()) {
            SpectrumProperty p = iterator.next();
            if (p.equals(property)){
                iterator.remove();
                return true;
            }
        }
        return false;
    }
    public static boolean isNotBadQuality(Ms2Experiment experiment){
        return experiment.getAnnotation(CompoundQuality.class, new CompoundQuality(SpectrumProperty.Good)).isGoodQuality();
    }

    public static boolean hasProperty(Ms2Experiment experiment, SpectrumProperty property) {
        CompoundQuality quality = experiment.getAnnotation(CompoundQuality.class);
        if (quality==null) return false;
        else return quality.hasProperty(property);
    }

    public static void setProperty(Ms2Experiment experiment, SpectrumProperty property){
        CompoundQuality quality = experiment.getAnnotation(CompoundQuality.class);
        if (quality==null){
            quality = new CompoundQuality(property);
            experiment.setAnnotation(CompoundQuality.class, quality);
        } else {
            quality.removeProperty(SpectrumProperty.Good);//all other properties are negative!
            quality.addProperty(property);
        }
    }

    public static boolean removeProperty(Ms2Experiment experiment, SpectrumProperty property) {
        CompoundQuality quality = experiment.getAnnotation(CompoundQuality.class);
        if (quality==null) return false;
        else return quality.hasProperty(property);
    }

    public static String getQualityString(Ms2Experiment experiment){
        CompoundQuality quality = experiment.getAnnotation(CompoundQuality.class);
        if (quality==null) return "";
        return quality.toString();
    }

    public static CompoundQuality fromString(String string) {
        String[] properties = string.split(",");
        CompoundQuality quality = new CompoundQuality();
        for (String property : properties) {
            quality.addProperty(SpectrumProperty.valueOf(property));
        }
        return quality;
    }

    public static List<SpectrumProperty> getProperties(Ms2Experiment experiment){
        CompoundQuality quality = experiment.getAnnotation(CompoundQuality.class);
        if (quality==null) return Collections.emptyList();
        return Collections.unmodifiableList(quality.getProperties());
    }

    public static SpectrumProperty[] getUsedProperties(Ms2Dataset dataset){
        return getUsedProperties(dataset.getExperiments());
    }

    public static SpectrumProperty[] getUsedProperties(List<Ms2Experiment> experiments){
        Set<SpectrumProperty> properties = new HashSet<>();
        for (Ms2Experiment experiment : experiments) {
            properties.addAll(CompoundQuality.getProperties(experiment));
        }
        return properties.toArray(new SpectrumProperty[0]);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<SpectrumProperty> iterator = properties.iterator();
        while (iterator.hasNext()) {
            SpectrumProperty next = iterator.next();
            builder.append(next.toString());
            if (iterator.hasNext()) builder.append(",");
        }
        return builder.toString();
    }
}
