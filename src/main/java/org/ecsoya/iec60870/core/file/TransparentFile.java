/*******************************************************************************
 * Copyright (C) 2019 Ecsoya (jin.liu@soyatec.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.ecsoya.iec60870.core.file;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * Simple implementation of IFileProvider that can be used to provide
 * transparent files. Derived classed should override the TransferComplete
 * method.
 */
public class TransparentFile implements IFileProvider {
	private ArrayList<byte[]> sections = new ArrayList<byte[]>();

	private LocalDateTime time = LocalDateTime.MIN;

	private int ca;
	private int ioa;
	private NameOfFile nof = NameOfFile.values()[0];

	public TransparentFile(int ca, int ioa, NameOfFile nof) {
		this.ca = ca;
		this.ioa = ioa;
		this.nof = nof;
		time = LocalDateTime.now();
	}

	public final void addSection(byte[] section) {
		sections.add(section);
	}

	@Override
	public final int getCommonAddress() {
		return ca;
	}

	@Override
	public final LocalDateTime getFileDate() {
		return time;
	}

	@Override
	public final int getFileSize() {
		int fileSize = 0;

		for (byte[] section : sections) {
			fileSize += section.length;
		}

		return fileSize;
	}

	@Override
	public final int getInformationObjectAddress() {
		return ioa;
	}

	@Override
	public final NameOfFile getNameOfFile() {
		return nof;
	}

	@Override
	public final int getSectionSize(int sectionNumber) {
		if (sectionNumber < sections.size()) {
			return sections.get(sectionNumber).length;
		} else {
			return -1;
		}
	}

	@Override
	public final boolean getSegmentData(int sectionNumber, int offset, int segmentSize, byte[] segmentData) {
		if ((sectionNumber >= sections.size()) || (sectionNumber < 0)) {
			return false;
		}

		byte[] section = sections.get(sectionNumber);

		if (offset + segmentSize > section.length) {
			return false;
		}

		for (int i = 0; i < segmentSize; i++) {
			segmentData[i] = section[i + offset];
		}

		return true;
	}

	@Override
	public void transferComplete(boolean success) {
	}
}
