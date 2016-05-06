/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.gradle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.diffplug.common.base.Throwing;

/** Utilities for mucking with zip files. */
public class ZipUtil {
	/**
	 * Reads the given entry from the zip.
	 * 
	 * @param input		a zip file
	 * @param toRead	a path within that zip file
	 * @param reader	will be called with an InputStream containing the contents of that entry in the zip file
	 */
	public static void read(File input, String toRead, Throwing.Specific.Consumer<InputStream, IOException> reader) throws IOException {
		try (
				ZipFile file = new ZipFile(input);
				InputStream stream = file.getInputStream(file.getEntry(toRead));) {
			reader.accept(stream);
		}
	}

	/**
	 * Modifies only the specified entries in a zip file. 
	 *
	 * @param input 		an input stream from a zip file
	 * @param output		an output stream to a zip file
	 * @param toModify		a map from path to an input stream for the entries you'd like to change
	 * @param toOmit		a set of entries you'd like to leave out of the zip
	 * @throws IOException
	 */
	public static void modify(InputStream input, OutputStream output, Map<String, InputStream> toModify, Set<String> toOmit) throws IOException {
		ZipInputStream zipInput = new ZipInputStream(input);
		ZipOutputStream zipOutput = new ZipOutputStream(output);

		while (true) {
			// read the next entry
			ZipEntry entry = zipInput.getNextEntry();
			if (entry == null) {
				break;
			}

			InputStream replacement = toModify.get(entry.getName());
			if (replacement != null) {
				// if it's the entry being modified, enter the modified stuff
				ZipEntry newEntry = new ZipEntry(entry.getName());
				newEntry.setComment(entry.getComment());
				newEntry.setExtra(entry.getExtra());
				newEntry.setMethod(entry.getMethod());
				newEntry.setTime(entry.getTime());

				zipOutput.putNextEntry(newEntry);
				copy(replacement, zipOutput);
				replacement.close();
			} else if (!toOmit.contains(entry.getName())) {
				// if it isn't being modified, just copy the file stream straight-up
				ZipEntry newEntry = new ZipEntry(entry);
				newEntry.setCompressedSize(-1);
				zipOutput.putNextEntry(newEntry);
				copy(zipInput, zipOutput);
			}

			// close the entries
			zipInput.closeEntry();
			zipOutput.closeEntry();
		}

		// close the streams
		zipInput.close();
		zipOutput.close();
	}

	/**
	 * Creates a single-entry zip file.
	 * 
	 * @param input					an uncompressed file
	 * @param pathWithinArchive		the path within the archive
	 * @param output				the new zip file it will be compressed into
	 */
	public static void zip(File input, String pathWithinArchive, File output) throws IOException {
		try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
			zipStream.setMethod(ZipOutputStream.DEFLATED);
			zipStream.setLevel(9);
			zipStream.putNextEntry(new ZipEntry(pathWithinArchive));
			try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(input))) {
				copy(inputStream, zipStream);
			}
		}
	}

	/** Copies one stream into the other. */
	private static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[1024];

		int numRead = input.read(buf);
		while (numRead > 0) {
			output.write(buf, 0, numRead);
			numRead = input.read(buf);
		}
	}
}
