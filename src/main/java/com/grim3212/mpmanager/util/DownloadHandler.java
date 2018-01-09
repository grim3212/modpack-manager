package com.grim3212.mpmanager.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;

public class DownloadHandler {

	public static File getZipFromLocation(String loc) {
		if (loc.endsWith(".zip")) {

			// Check if it is a link
			if (loc.startsWith("http")) {
				// Download into temp directory
				return downloadFile(System.getProperty("java.io.tmpdir"), loc);
			} else {
				// Returns new file with the zip as it's location
				return new File(loc);
			}

		} else if (loc.startsWith("https://minecraft.curseforge.com/projects/") || loc.startsWith("https://www.feed-the-beast.com")) {
			// Download zip to temp directory
			return downloadFile(System.getProperty("java.io.tmpdir"), fixUrl(loc));
		} else {
			System.err.println(loc + " is not a valid modpack zip!!");
			return null;
		}

	}

	public static String fixUrl(String url) {
		if (url.endsWith("download")) {
			return url;
		} else {
			return url + "/download";
		}
	}

	public static File downloadFile(String path, String link) {
		return downloadFile(path, link, null);
	}

	public static File downloadFile(String path, String link, DownloadProgress onProgress) {
		try {
			URL website = new URL(link);
			HttpURLConnection httpConn = (HttpURLConnection) website.openConnection();
			int responseCode = httpConn.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {

				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");
				int size = httpConn.getContentLength();

				// Get the filename
				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = FilenameUtils.getName(httpConn.getURL().toURI().getPath());
				}

				// Then check to see if file already exists at the specified location
				File download = new File(path + File.separator + fileName);

				if (onProgress != null)
					onProgress.fileName = fileName;

				if (download.exists()) {
					if (download.length() == size) {
						// Don't need to download another one
						System.out.println("File already exists, Don't need to download a new version");
					} else {
						// Download a new file
						performDownload(download, httpConn, onProgress);
					}
				} else {
					// We download the file
					performDownload(download, httpConn, onProgress);
				}

				// Return the new file download
				return download;
			} else {
				System.err.println("Failed download for " + link + ". Got status code = " + responseCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Failed to download a file
		return null;
	}

	private static void performDownload(File output, HttpURLConnection conn, DownloadProgress onProgress) throws Exception {
		long completeFileSize = conn.getContentLength();

		BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
		FileOutputStream fos = new FileOutputStream(output);
		BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
		byte[] data = new byte[1024];
		long downloadedFileSize = 0;
		int x = 0;
		while ((x = in.read(data, 0, 1024)) >= 0) {
			downloadedFileSize += x;

			// calculate progress
			final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);

			if (onProgress != null) {
				onProgress.run(currentProgress);
			}

			bout.write(data, 0, x);
		}
		bout.close();
		in.close();
	}
}
