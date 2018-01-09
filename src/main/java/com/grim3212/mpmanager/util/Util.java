package com.grim3212.mpmanager.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.stream.JsonReader;
import com.grim3212.mpmanager.ModPackManager;
import com.grim3212.mpmanager.json.ModPack;

public class Util {

	public static String getResource(String toConvert) {
		return Thread.currentThread().getContextClassLoader().getResource(toConvert).toString();
	}

	public static boolean checkAndCreateDir(File folderPath) {
		if (!folderPath.exists()) {
			return folderPath.mkdirs();
		}

		return true;
	}

	public static List<ModPack> getCurrentPacks() {
		List<ModPack> packs = new ArrayList<ModPack>();

		Path instancePath = Paths.get(ModPackManager.settings.instanceLocation.getAsString());
		try {
			List<Path> folders = Files.walk(instancePath, 1).filter(Files::isDirectory).collect(Collectors.toList());

			for (Path p : folders) {
				Path manifest = p.resolve("manifest.json");

				if (Files.exists(manifest)) {
					JsonReader reader = new JsonReader(new FileReader(manifest.toFile()));
					ModPack pack = ModPackManager.gson.fromJson(reader, ModPack.class);

					if (pack.modpackManagerVersion == 1) {
						pack.givenName = manifest.getParent().getFileName().toString();
						pack.folderPath = manifest.getParent().toString();

						// Add the pack to our list of viable modpacks
						packs.add(pack);
					}
				}
			}

		} catch (IOException e) {
			System.err.println("Failed to get modpacks!");
			e.printStackTrace();
		}

		return packs;
	}
}
