package org.ecsoya.iec60870.conn;

import java.time.LocalDateTime;

import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

public interface IFileProvider {

	/**
	 * Returns the CA (Comman address) of the file
	 * 
	 * @return The CA
	 */
	int GetCA();

	/**
	 * Returns the IOA (information object address of the file)
	 * 
	 * @return The IOA
	 */
	int GetIOA();

	NameOfFile GetNameOfFile();

	LocalDateTime GetFileDate();

	/**
	 * Gets the size of the file in bytes
	 * 
	 * @return The file size in bytes
	 */
	int GetFileSize();

	/**
	 * Gets the size of a section in byzes
	 * 
	 * @return The section size in bytes or -1 if the section does not exist
	 * @param sectionNumber Number of section (starting with 0)
	 */
	int GetSectionSize(int sectionNumber);

	/**
	 * Gets the segment data.
	 * 
	 * @return <c>true</c>, if segment data was gotten, <c>false</c> otherwise.
	 * @param sectionNumber Section number.
	 * @param offset        Offset.
	 * @param segmentSize   Segment size.
	 * @param segmentData   Segment data.
	 */
	boolean GetSegmentData(int sectionNumber, int offset, int segmentSize, byte[] segmentData);

	/**
	 * Indicates that the transfer is complete. When success equals true the file
	 * data can be deleted
	 * 
	 * @param success If set to <c>true</c> success.
	 */
	void TransferComplete(boolean success);
}