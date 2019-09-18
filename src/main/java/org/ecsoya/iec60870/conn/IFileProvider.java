package org.ecsoya.iec60870.conn;

import java.time.LocalDateTime;

import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

public interface IFileProvider {

	/**
	 * Returns the CA (Comman address) of the file
	 * 
	 * @return The CA
	 */
	int getCommonAddress();

	LocalDateTime getFileDate();

	/**
	 * Gets the size of the file in bytes
	 * 
	 * @return The file size in bytes
	 */
	int getFileSize();

	/**
	 * Returns the IOA (information object address of the file)
	 * 
	 * @return The IOA
	 */
	int getInformationObjectAddress();

	NameOfFile getNameOfFile();

	/**
	 * Gets the size of a section in byzes
	 * 
	 * @return The section size in bytes or -1 if the section does not exist
	 * @param sectionNumber Number of section (starting with 0)
	 */
	int getSectionSize(int sectionNumber);

	/**
	 * Gets the segment data.
	 * 
	 * @return <c>true</c>, if segment data was gotten, <c>false</c> otherwise.
	 * @param sectionNumber Section number.
	 * @param offset        Offset.
	 * @param segmentSize   Segment size.
	 * @param segmentData   Segment data.
	 */
	boolean getSegmentData(int sectionNumber, int offset, int segmentSize, byte[] segmentData);

	/**
	 * Indicates that the transfer is complete. When success equals true the file
	 * data can be deleted
	 * 
	 * @param success If set to <c>true</c> success.
	 */
	void transferComplete(boolean success);
}