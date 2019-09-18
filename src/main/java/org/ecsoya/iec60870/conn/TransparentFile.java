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
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private List<byte[]> sections = new List<byte[]>();
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void AddSection(byte[] section)
	public final void AddSection(byte[] section) {
		sections.add(section);
	}

	public final int GetCA() {
		return ca;
	}

	public final int GetIOA() {
		return ioa;
	}

	public final NameOfFile GetNameOfFile() {
		return nof;
	}

	public final LocalDateTime GetFileDate() {
		return time;
	}

	public final int GetFileSize() {
		int fileSize = 0;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (byte[] section in sections)
		for (byte[] section : sections) {
			fileSize += section.length;
		}

		return fileSize;
	}

	public final int GetSectionSize(int sectionNumber) {
		if (sectionNumber < sections.size()) {
			return sections.get(sectionNumber).length;
		} else {
			return -1;
		}
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public bool GetSegmentData(int sectionNumber, int offset, int segmentSize, byte[] segmentData)
	public final boolean GetSegmentData(int sectionNumber, int offset, int segmentSize, byte[] segmentData) {
		if ((sectionNumber >= sections.size()) || (sectionNumber < 0)) {
			return false;
		}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] section = sections [sectionNumber];
		byte[] section = sections.get(sectionNumber);

		if (offset + segmentSize > section.length) {
			return false;
		}

		for (int i = 0; i < segmentSize; i++) {
			segmentData[i] = section[i + offset];
		}

		return true;
	}

	public void TransferComplete(boolean success) {
	}
}