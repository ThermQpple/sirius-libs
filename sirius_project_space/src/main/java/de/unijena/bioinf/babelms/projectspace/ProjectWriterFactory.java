package de.unijena.bioinf.babelms.projectspace;

public interface ProjectWriterFactory {

    public ProjectWriter getSiriusOutputWriter(String sirius, DirectoryWriter.WritingEnvironment env);

    public ProjectWriter getDirectoryOutputWriter(String sirius, DirectoryWriter.WritingEnvironment env);

}
