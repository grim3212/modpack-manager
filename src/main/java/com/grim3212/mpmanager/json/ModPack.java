package com.grim3212.mpmanager.json;

import java.util.List;

public class ModPack {

	// Default props
	public Minecraft minecraft;
	public String manifestType;
	public String manifestVersion;
	public String name;
	public String author;
	public String version;
	public int projectID;

	// Should never be any duplicate project ids
	public List<Mod> files;
	public String overrides;

	// ModPack Manager added props
	public int modpackManagerVersion;
	public String origin;
	public String sourceLocation;
	public transient String folderPath;
	public transient String givenName;
	public List<String> overrideFiles;

	public boolean modContained(int projectID) {
		for (Mod mod : files) {
			if (mod.projectID == projectID) {
				return true;
			}
		}

		return false;
	}

	public Mod getMod(int projectID) {
		for (Mod mod : files) {
			if (mod.projectID == projectID) {
				return mod;
			}
		}

		System.err.println("Request mod with id " + projectID + " does not exist!");
		return null;
	}
}
