/**
 * 
 */
package com.git.gdsbuilder.validator.fileReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @className DownloadValidateFile.java
 * @description
 * @author DY.Oh
 * @since 2018. 2. 13. 오후 4:38:32
 */
public class DownloadValidateFile {

	private static final int BUFFER_SIZE = 4096;

	public boolean download(String path, String zipfilePath) throws IOException {

		URL url = new URL(path);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

		int responseCode = httpConn.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");
			if (disposition != null) {
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 9, disposition.length());
				}
			} else {
				fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
			}
			InputStream inputStream = httpConn.getInputStream();
			String saveFilePath = zipfilePath + File.separator + URLDecoder.decode(fileName, "UTF-8");
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);
			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			httpConn.disconnect();
			outputStream.close();
			inputStream.close();
			return true;
		} else {
			return false;
		}
		
	}
}
