package com.grim3212.mpmanager.json;

import java.util.List;

public class Minecraft {

	public String version;
	public List<ModLoader> modLoaders;

	public String getForgeVersion() {
		for (ModLoader loader : modLoaders) {
			if (loader.id.startsWith("forge-")) {
				return loader.id.replace("forge-", "");
			}
		}

		// Forge version doesn't exist
		return "";
	}
}
