package org.ecsoya.iec60870.conn;

import java.util.ArrayList;
import java.util.List;

import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.ie.FileDirectory;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

public class FilesAvailable {
	public static class CS101n104File {

		public CS101n104File(IFileProvider file) {
			this.provider = file;
		}

		public IFileProvider provider = null;
		public Object selectedBy = null;

	}

	private List<CS101n104File> availableFiles = new ArrayList<CS101n104File>();

	public CS101n104File GetFile(int ca, int ioa, NameOfFile nof) {
		synchronized (availableFiles) {

			for (CS101n104File file : availableFiles) {
				if ((file.provider.GetCA() == ca) && (file.provider.GetIOA() == ioa)) {

					if (nof == NameOfFile.DEFAULT)
						return file;
					else {

						if (nof == file.provider.GetNameOfFile())
							return file;
					}
				}
			}
		}

		return null;
	}

	void SendDirectoy(IMasterConnection masterConnection, boolean spontaneous) {
		CauseOfTransmission cot;

		if (spontaneous)
			cot = CauseOfTransmission.SPONTANEOUS;
		else
			cot = CauseOfTransmission.REQUEST;

		synchronized (availableFiles) {

			int size = availableFiles.size();
			int i = 0;

			int currentCa = -1;
			int currentIOA = -1;

			ASDU directoryAsdu = null;

			for (CS101n104File file : availableFiles) {

				boolean newAsdu = false;

				if (file.provider.GetCA() != currentCa) {
					currentCa = file.provider.GetCA();
					newAsdu = true;
				}

				if (currentIOA != (file.provider.GetIOA() - 1)) {
					newAsdu = true;
				}

				if (newAsdu) {
					if (directoryAsdu != null) {
						masterConnection.SendASDU(directoryAsdu);
						directoryAsdu = null;
					}
				}

				currentIOA = file.provider.GetIOA();

				i++;

				if (directoryAsdu == null) {
					System.out.println("Send directory ASDU");
					directoryAsdu = new ASDU(masterConnection.GetApplicationLayerParameters(), cot, false, false,
							(byte) 0, currentCa, true);
				}

				boolean lastFile = (i == size);

				byte sof = 0;

				if (lastFile)
					sof = 0x20;

				InformationObject io = new FileDirectory(currentIOA, file.provider.GetNameOfFile(),
						file.provider.GetFileSize(), sof, new CP56Time2a(file.provider.GetFileDate()));

				directoryAsdu.addInformationObject(io);
			}

			if (directoryAsdu != null) {

				System.out.println("Send directory ASDU");
				masterConnection.SendASDU(directoryAsdu);
			}

		}
	}

	public void AddFile(IFileProvider file) {
		synchronized (availableFiles) {

			availableFiles.add(new CS101n104File(file));
		}

	}

	public void RemoveFile(IFileProvider file) {
		synchronized (availableFiles) {

			for (CS101n104File availableFile : availableFiles) {

				if (availableFile.provider == file) {
					availableFiles.remove(availableFile);
					return;
				}

			}
		}
	}

}