package com.grim3212.mpmanager.util;

public abstract class DownloadProgress {

	public String fileName = "";

	public abstract void run(int progress);
}
