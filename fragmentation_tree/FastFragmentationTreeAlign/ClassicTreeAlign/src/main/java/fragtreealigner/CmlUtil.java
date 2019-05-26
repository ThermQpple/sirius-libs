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
package fragtreealigner;

import fragtreealigner.domainobjects.graphs.FragmentationTree;
import fragtreealigner.util.Parameters;
import fragtreealigner.util.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CmlUtil {
	public static void main(String[] args) {
		if (args.length == 0) return;
		if (args[0].equalsIgnoreCase("dotToCml")) dotToCml(args[1]);
	}
	
	public static void dotToCml(String arg) {
		Session session = new Session();
		Parameters params = new Parameters(session);
		session.setParameters(params);

		File fileOrDirectory = new File(arg);
		File[] files;
		if (fileOrDirectory.isDirectory()) files = fileOrDirectory.listFiles();
		else files = new File[]{fileOrDirectory};
		
		String filename;
		FragmentationTree fragTree;
		for (File file : files) {
			filename = file.getAbsolutePath();
			if (!filename.endsWith(".dot")) continue;
			try {
				fragTree = FragmentationTree.readFromDot(FileUtils.ensureBuffering(new FileReader(filename)), session);
				if (fragTree == null){
					System.err.println("Tree empty in file: "+filename);
				} else {
				fragTree.writeToCml(filename.replaceAll(".dot$", "") + ".cml");
				}
			} catch (Exception e) {
				System.err.println("The following error occured while processing the files:\n" + e.getMessage());
			}
		}
	}
}
