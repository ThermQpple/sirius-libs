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
package de.unijena.bioinf.ChemistryBase.ms.inputValidators;

import org.slf4j.LoggerFactory;

/**
 * Can be implemented by logger, PrintStreams or others to track warnings in the validation process
 */
public interface Warning {

    Warning Logger = new Warning() {
        org.slf4j.Logger logger = LoggerFactory.getLogger(Warning.class);
        @Override
        public void warn(String message) {
            logger.warn(message);
        }
    };

    class Noop implements Warning{

        @Override
        public void warn(String message) {
            // dead code!
        }
    }

    void warn(String message);

}
