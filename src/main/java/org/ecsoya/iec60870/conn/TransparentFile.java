package org.ecsoya.iec60870.conn;

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

	public final int getCommonAddress() {
		return ca;
	}

	public final LocalDateTime getFileDate() {
		return time;
	}

	public final int getFileSize() {
		int fileSize = 0;

		for (byte[] section : sections) {
			fileSize += section.length;
		}

		return fileSize;
	}

	public final int getInformationObjectAddress() {
		return ioa;
	}

	public final NameOfFile getNameOfFile() {
		return nof;
	}

	public final int getSectionSize(int sectionNumber) {
		if (sectionNumber < sections.size()) {
			return sections.get(sectionNumber).length;
		} else {
			return -1;
		}
	}

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

	public void transferComplete(boolean success) {
	}
}