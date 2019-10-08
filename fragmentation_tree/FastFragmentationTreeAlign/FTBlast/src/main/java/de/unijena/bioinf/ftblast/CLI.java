/*
 *  This file is part of the SIRIUS library for analyzing MS and MS/MS data
 *
 *  Copyright (C) 2013-2015 Kai Dührkop
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with SIRIUS.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.unijena.bioinf.ftblast;

import au.com.bytecode.opencsv.CSVReader;
import de.unijena.bioinf.ChemistryBase.data.DoubleDataMatrix;
import de.unijena.bioinf.ChemistryBase.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kaidu on 19.07.2014.
 */
public class CLI {

    /*
    Usage:
    ftblast matrix1 matrix2 matrix3
     */
    public static void main(String[] args) {
        final List<Iterator<String[]>> csvParser = new ArrayList<Iterator<String[]>>();
        final ArrayList<String> names = new ArrayList<String>();
        final ArrayList<CSVReader> readers = new ArrayList<CSVReader>();
        for (String arg : args) {
            final File f = new File(arg);
            try {
                final CSVReader fileReader = new CSVReader(FileUtils.ensureBuffering(new FileReader(f)));
                names.add(f.getName().substring(0, f.getName().lastIndexOf('.')));
                final String[] headerLine = fileReader.readNext();
                csvParser.add(new Iterator<String[]>() {
                    String[] line = headerLine;

                    @Override
                    public boolean hasNext() {
                        return line != null;
                    }

                    @Override
                    public String[] next() {
                        final String[] ret = line;
                        try {
                            line = fileReader.readNext();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return ret;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                });
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open " + f.toString() + ":\n" + e);
            } catch (IOException e) {
                System.err.println("Cannot open " + f.toString() + ":\n" + e);
            }
        }
        final DoubleDataMatrix matrix = DoubleDataMatrix.overlayIntersection(csvParser, names, null);

    }

}
