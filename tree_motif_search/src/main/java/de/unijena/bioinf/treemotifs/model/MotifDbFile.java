package de.unijena.bioinf.treemotifs.model;

import de.unijena.bioinf.ms.annotations.Ms2ExperimentAnnotation;
import de.unijena.bioinf.ms.properties.DefaultProperty;

public class MotifDbFile implements Ms2ExperimentAnnotation {
    @DefaultProperty(propertyParent = "experimentalMotifDb")
    public String motifDB;

    public MotifDbFile() {
        this.motifDB = "";
    }
}
