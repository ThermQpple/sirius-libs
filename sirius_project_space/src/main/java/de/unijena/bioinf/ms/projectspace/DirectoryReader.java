package de.unijena.bioinf.ms.projectspace;

import de.unijena.bioinf.ChemistryBase.chem.MolecularFormula;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.ft.FTree;
import de.unijena.bioinf.babelms.Index;
import de.unijena.bioinf.babelms.json.FTJsonReader;
import de.unijena.bioinf.babelms.ms.JenaMsParser;
import de.unijena.bioinf.sirius.ExperimentResult;
import de.unijena.bioinf.sirius.IdentificationResult;

import java.io.*;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.unijena.bioinf.ms.projectspace.DirectoryWriter.logger;

public class DirectoryReader implements ProjectReader {

    public interface ReadingEnvironment {

        List<String> list();

        void enterDirectory(String name) throws IOException;

        /**
         * test if is this child a directory
         * @param name
         * @return
         */
        boolean isDirectory(String name);

        InputStream openFile(String name) throws IOException;

        URL absolutePath(String name) throws IOException;

        void closeFile() throws IOException;

        void leaveDirectory() throws IOException;

        void close() throws IOException;
    }

    protected static class Instance {
        protected String directory;
        protected Instance(String directory) {
            this.directory = directory;
        }

        protected String getDirectory() {
            return directory;
        }
    }

    protected final ReadingEnvironment env;
    protected final List<Instance> experiments;
    protected int currentIndex = 0;

    public DirectoryReader(ReadingEnvironment env) {
        this.env = env;
        List<String> names = env.list();
        this.experiments = new ArrayList<>(names.size());
        for (String name : names) {
            try {
                if (!containsSpectrumMS(env, name)) continue;
            } catch (IOException e) {
                throw new RuntimeException("Cannot read directory: "+e.getMessage(), e);
            }
            experiments.add(new Instance(name));
        }
    }

    private boolean containsSpectrumMS(ReadingEnvironment env, String dirName) throws IOException {
        if (!env.isDirectory(dirName)) return false;
        env.enterDirectory(dirName);
        boolean hasSpectrum = false;
        for (String file : env.list()) {
            if (file.equals("spectrum.ms")) hasSpectrum = true;
        }
        env.leaveDirectory();
        return hasSpectrum;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean hasNext() {
        return currentIndex < experiments.size();
    }

    @Override
    public ExperimentResult next() {
        if (!hasNext()) throw new NoSuchElementException();
        try {
            return parseExperiment(experiments.get(currentIndex));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ++currentIndex;
        }
    }

    private final static Pattern RESULT_PATTERN = Pattern.compile("(\\d+)_(.+)(_(.+))?\\.json");
    private final static Pattern INDEX_PATTERN = Pattern.compile("^(\\d+)_");
    private ExperimentResult parseExperiment(final Instance instance) throws IOException {
        env.enterDirectory(instance.getDirectory());
        final HashSet<String> names = new HashSet<>(env.list());
        Ms2Experiment input = null;
        final List<IdentificationResult> results = new ArrayList<>();
        // read spectrum
        if (names.contains("spectrum.ms")) {
            input = parseSpectrum(instance);
        }
        boolean hasIndex = true;
        if (!input.hasAnnotation(Index.class)){
            //fallback for older versions
            hasIndex = false;
            Matcher matcher = INDEX_PATTERN.matcher(instance.getDirectory());
            if (matcher.matches()){
                input.setAnnotation(Index.class, new Index(Integer.parseInt(matcher.group(1))));
            } else {
                logger.warn("Cannot parse index for compound in directory "+instance.getDirectory());
            }
        }
        // read trees
        if (names.contains("trees")) {
            env.enterDirectory("trees");
            final List<String> trs = env.list();
            keep(trs, RESULT_PATTERN);
            for (final String s : trs) {
                Matcher m = RESULT_PATTERN.matcher(s);
                m.matches();
                final MolecularFormula formula = MolecularFormula.parse(m.group(2));
                final int rank = Integer.parseInt(m.group(1));
                final FTree tree = read(s, new Do<FTree>() {
                    @Override
                    public FTree run(Reader r) throws IOException {
                        return new FTJsonReader().parse(new BufferedReader(r), env.absolutePath(instance.getDirectory() + "/trees/" + s));
                    }
                });
                results.add(new IdentificationResult(tree, rank));
            }
            env.leaveDirectory();
        }
        Collections.sort(results, new Comparator<IdentificationResult>() {
            @Override
            public int compare(IdentificationResult o1, IdentificationResult o2) {
                return Integer.compare(o1.getRank(), o2.getRank());
            }
        });
        addMetaData(input, results);
        env.leaveDirectory();
        if (input.getSource()!=null && hasIndex){
            return new ExperimentResult(input, results);
        } else {
            //fallback for older versions
            String[] nameSplit = instance.getDirectory().split("_");
            String source = nameSplit.length>1?nameSplit[1]:"";
            String name = nameSplit.length>2?nameSplit[2]:"unknown";
            return new ExperimentResult(input, results, source, name);

        }

    }

    protected void addMetaData(Ms2Experiment input, List<IdentificationResult> results) throws IOException {

    }

    private void keep(List<String> trs, Pattern p) {
        Iterator<String> i = trs.iterator();
        while (i.hasNext()) {
            if (!p.matcher(i.next()).matches()) {
                i.remove();
            }
        }
    }

    private Ms2Experiment parseSpectrum(final Instance i) throws IOException {
        return read("spectrum.ms", new Do<Ms2Experiment>() {
            @Override
            public Ms2Experiment run(Reader r) throws IOException {
//                return new JenaMsParser().parse(new BufferedReader(r), env.absolutePath(i.getDirectory() + "/" + i.fileName + ".ms"));
                return new JenaMsParser().parse(new BufferedReader(r), env.absolutePath(i.getDirectory() + "/spectrum.ms"));
            }
        });
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }


    protected interface Do<T> {
        T run(Reader r) throws IOException;
    }

    protected <T> T read(String name, DirectoryReader.Do<T> f)throws IOException  {
        final
        InputStream stream = env.openFile(name);
        try {
            final BufferedReader inReader = new BufferedReader(new InputStreamReader(stream));
            try {
                return f.run(new DirectoryReader.DoNotCloseReader(inReader));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        } finally {
            env.closeFile();
        }
    }

    private static class DoNotCloseReader extends Reader {

        protected final Reader r;

        private DoNotCloseReader(Reader r) {
            this.r = r;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return r.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            // ignore
        }

        @Override
        public int read(CharBuffer target) throws IOException {
            return r.read(target);
        }

        @Override
        public int read() throws IOException {
            return r.read();
        }

        @Override
        public int read(char[] cbuf) throws IOException {
            return r.read(cbuf);
        }

        @Override
        public long skip(long n) throws IOException {
            return r.skip(n);
        }

        @Override
        public boolean ready() throws IOException {
            return r.ready();
        }

        @Override
        public boolean markSupported() {
            return r.markSupported();
        }

        @Override
        public void mark(int readAheadLimit) throws IOException {
            r.mark(readAheadLimit);
        }

        @Override
        public void reset() throws IOException {
            r.reset();
        }
    }
}
