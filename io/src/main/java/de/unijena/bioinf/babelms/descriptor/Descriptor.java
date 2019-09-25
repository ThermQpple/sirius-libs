package de.unijena.bioinf.babelms.descriptor;

import de.unijena.bioinf.ChemistryBase.data.DataDocument;
import de.unijena.bioinf.ms.annotations.DataAnnotation;

/**
 * This class handles the serialization of annotation objects.
 * As I do not have a final API for this yet, annotation objects are serialized in a hardcoded manner.
 * However, future versions might allow other APIs to define their own serialization routines
 * Until this point every user is encouraged to define his own Annotation classes in the ChemistryBase packacke as
 * final, immutable pojos together with a serialization route in this class.
 */
public interface Descriptor<AnnotationType extends DataAnnotation> {

    /**
     * A Descriptor is tried to parse an annotation as soon as one of the keywords appear in the dictionary.
     * If the keyword list is empty, the descriptor is always used.
     *
     * @return a list of keywords.
     */
    String[] getKeywords();

    Class<AnnotationType> getAnnotationClass();

    <G, D, L> AnnotationType read(DataDocument<G, D, L> document, D dictionary);

    <G, D, L> void write(DataDocument<G, D, L> document, D dictionary, AnnotationType annotation);

}
