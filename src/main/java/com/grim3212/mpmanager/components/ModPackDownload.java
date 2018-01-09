package com.grim3212.mpmanager.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import com.google.gson.GsonBuilder;
import com.grim3212.mpmanager.ModPackManager;
import com.grim3212.mpmanager.json.Mod;
import com.grim3212.mpmanager.json.ModPack;
import com.grim3212.mpmanager.util.DownloadHandler;
import com.grim3212.mpmanager.util.DownloadProgress;
import com.grim3212.mpmanager.util.Util;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ModPackDownload {

	private Stage dialog;
	private Stage parent;
	private String name;
	private String url;
	private Label currentLbl;
	private Label totalLbl;
	private ProgressBar currentItemProgress;
	private ProgressBar totalItemsProgress;

	private ModPack oldPack = null;

	public ModPackDownload(Stage parent, String name, String url) {
		this.parent = parent;
		this.url = url;
		this.name = name;
	}

	public ModPackDownload(Stage parent, ModPack oldPack, String updateUrl) {
		this.parent = parent;
		this.url = updateUrl;
		this.name = oldPack.givenName;
		this.oldPack = oldPack;
	}

	public void startDownload() {
		dialog = new Stage();

		dialog.initOwner(this.parent);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle(oldPack != null ? "Updating " + this.name : "Downloading modpack...");
		dialog.setResizable(false);
		dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				// Don't allow to be closed
				event.consume();
			}
		});

		BorderPane border = new BorderPane();
		border.setPadding(new Insets(10, 10, 10, 10));

		VBox container = new VBox(10);
		container.setAlignment(Pos.TOP_CENTER);

		currentLbl = new Label("");
		currentItemProgress = new ProgressBar(0);
		currentItemProgress.prefWidthProperty().bind(container.widthProperty().subtract(20));

		totalLbl = new Label("");
		totalItemsProgress = new ProgressBar(0);
		totalItemsProgress.prefWidthProperty().bind(container.widthProperty().subtract(20));

		container.getChildren().add(currentLbl);
		container.getChildren().add(currentItemProgress);
		container.getChildren().add(totalLbl);
		container.getChildren().add(totalItemsProgress);

		border.setCenter(container);

		Scene addInstanceScene = new Scene(border, 300, 145);
		addInstanceScene.getStylesheets().add(Util.getResource("stylesheets/application.css"));

		// Add everything to the dialog and display it
		dialog.setScene(addInstanceScene);
		dialog.show();

		ModPackManager.selectedTab.select(1);

		// When we are done setting up the scene move on to the actual downloading

		// First we need to grab the zip that we are going to process

		(new Thread("ModPack Downloader") {
			public void run() {
				File zip = DownloadHandler.getZipFromLocation(url);

				if (zip != null && zip.exists()) {
					processZip(zip);
				} else {
					Alerts.createErrorAlert("Failed to get zip file", "Zip file was not found", "The zip at " + url + " was not able to be found or downloaded!");
				}

				// Close the dialog
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						dialog.close();

						// Refresh the modpacks
						ModPackManager.home.refreshModPacks();
					}
				});

			};
		}).start();
	}

	public void processZip(File file) {
		try {
			System.out.println("Processing zip at " + file.getAbsolutePath());

			ZipFile zip = new ZipFile(file);

			Enumeration<? extends ZipEntry> entries = zip.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				// We want to get the manifest
				if (entry.getName().equals("manifest.json")) {
					InputStream stream = zip.getInputStream(entry);
					String manifest = IOUtils.toString(stream, Charset.defaultCharset());
					// Close the input stream
					stream.close();

					// Get the modpack from json
					ModPack pack = ModPackManager.gson.fromJson(manifest, ModPack.class);

					// Start populating the new fields
					pack.givenName = this.name;
					pack.origin = this.url;
					pack.folderPath = ModPackManager.settings.instanceLocation.getAsString() + File.separator + this.name;
					pack.sourceLocation = file.getAbsolutePath();
					pack.modpackManagerVersion = 1;

					// Remove old stuff if we are going to update

					if (oldPack != null) {

						// First remove the mods
						this.removeMods(oldPack, pack);

						// Then remove the overrides
						this.removeOverrides(oldPack);

						// After it goes back to being a basic modpack download
					}

					// Now we want to download all of the mods
					if (this.downloadFiles(pack)) {
						// Downloading files succeeded
						this.copyOverrides(pack);
						this.setupMMC(pack);
						this.writeManifest(pack);

					} else {
						// Downloading files failed
					}

					break;
				}
			}

			// Make sure we close the zip
			zip.close();
		} catch (Exception e) {
			Alerts.createErrorAlert("Error while processing zip file", "Something went wrong during downloading", e.getLocalizedMessage());
		}
	}

	public boolean downloadFiles(ModPack pack) {
		File modsFolder = new File(pack.folderPath + File.separator + "minecraft" + File.separator + "mods");

		if (Util.checkAndCreateDir(modsFolder)) {
			System.out.println("Created mods folder succesfully");

			int completed = 0;
			List<Mod> errored = new ArrayList<Mod>();

			for (Mod mod : pack.files) {
				String url = "https://minecraft.curseforge.com/projects/" + mod.projectID + "/files/" + mod.fileID + "/download";
				mod.url = url;

				File downloadedMod = DownloadHandler.downloadFile(modsFolder.getAbsolutePath(), url, new DownloadProgress() {

					@Override
					public void run(int progress) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								// Update current progress while downloading
								currentLbl.setText(fileName);
								currentItemProgress.setProgress(progress / 100d);
							}
						});
					}
				});

				// When finished we still want to update this
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// Update current progress while downloading
						if (downloadedMod != null && downloadedMod.exists())
							currentLbl.setText(downloadedMod.getName());
						currentItemProgress.setProgress(1);
					}
				});

				// Update total progress after download has finished
				if (downloadedMod != null && downloadedMod.exists()) {
					completed++;
					final int comp = completed;

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							totalLbl.setText(comp + " / " + pack.files.size());
							totalItemsProgress.setProgress(((double) comp) / pack.files.size());
						}
					});
					mod.success = true;
					mod.fileName = downloadedMod.getName();

					System.out.println(mod.projectID + " downloaded successfully!");
				} else {
					if (ModPackManager.settings.ignoreFailedDownloads.getAsBoolean()) {
						completed++;
						final int comp = completed;
						errored.add(mod);

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								totalLbl.setText(comp + " / " + pack.files.size());
								totalItemsProgress.setProgress(((double) comp) / pack.files.size());
							}
						});
						System.err.println("Failed to download " + mod.projectID + " at " + mod.url);
					} else {
						// Stop downloading if a file failed and we don't want to ignore them
						Alerts.createErrorAlert("Failed to download a mod", null, "Failed to download " + mod.projectID + " at " + mod.url);
						return false;
					}
				}
			}

			// Print out how many downloads failed
			if (!errored.isEmpty()) {
				System.out.println(errored.size() + " mods failed to download!");
			}

			// All files downloaded
			return true;
		} else {
			Alerts.createErrorAlert("Failed to create folder", "Folder creation failed", "Failed to create mods folder at " + modsFolder.getAbsolutePath());
		}
		return false;
	}

	public void copyOverrides(ModPack pack) {
		try {
			ZipFile zip = new ZipFile(pack.sourceLocation);
			Enumeration<? extends ZipEntry> entries = zip.entries();

			String overridesName = pack.overrides + "/";

			List<String> overrides = new ArrayList<String>();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String entryName = entry.getName();

				// We copy the entire overrides folder
				if (entryName.startsWith(overridesName)) {
					// Do we need to create a directory ?
					File file = new File(pack.folderPath + File.separator + "minecraft" + File.separator + entryName.replace(overridesName, ""));
					if (entryName.endsWith("/")) {
						file.mkdirs();
						continue;
					}

					File parent = file.getParentFile();
					if (parent != null) {
						parent.mkdirs();
					}

					// Extract the file
					InputStream is = zip.getInputStream(entry);
					FileOutputStream fos = new FileOutputStream(file);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = is.read(bytes)) >= 0) {
						fos.write(bytes, 0, length);
					}
					is.close();
					fos.close();

					// Add only files to the overrides
					if (file.exists() && file.isFile()) {
						overrides.add(entryName.replace(overridesName, ""));
					}

				}
			}
			// Make sure we close the zip
			zip.close();

			// Give the pack it's override files
			pack.overrideFiles = overrides;

		} catch (Exception e) {
			Alerts.createErrorAlert("Error while processing zip file", "Something went wrong while copying overrides", e.getLocalizedMessage());
		}
	}

	public void setupMMC(ModPack pack) {
		File cfg = new File(pack.folderPath + File.separator + "instance.cfg");

		try {
			if (!cfg.exists()) {
				PrintWriter writer = new PrintWriter(cfg, "UTF-8");

				writer.println("InstanceType=OneSix");
				writer.println("IntendedVersion=" + pack.minecraft.version);
				writer.println("ForgeVersion=" + pack.minecraft.getForgeVersion());
				writer.println("LogPrePostOutput=true");
				writer.println("OverrideCommands=false");
				writer.println("OverrideConsole=false");
				writer.println("OverrideJavaArgs=false");
				writer.println("OverrideJavaLocation=false");
				writer.println("OverrideMemory=false");
				writer.println("OverrideWindow=false");
				writer.println("iconKey=default");
				writer.println("lastLaunchTime=0");
				writer.println("name=" + pack.givenName);
				writer.println("totalTimePlayed=0");

				writer.close();

				System.out.println("MultiMC instance.cfg created successfully!");
			} else {
				System.out.println("MultiMC instance.cfg already exists!");

				// Read in line by line
				List<String> lines = Files.readAllLines(Paths.get(cfg.getPath()));

				// Create printwriter after we get lines so we don't override it
				PrintWriter writer = new PrintWriter(cfg, "UTF-8");

				for (String line : lines) {
					if (line.startsWith("IntendedVersion=")) {
						line = "IntendedVersion=" + pack.minecraft.version;
					}

					if (line.startsWith("ForgeVersion=")) {
						line = "ForgeVersion=" + pack.minecraft.getForgeVersion();
					}

					// Print out each line after modifying some
					writer.println(line);
				}

				// Close it
				writer.close();

				System.out.println("MultiMC instance.cfg updated successfully!");
			}
		} catch (Exception e) {
			System.err.println("Failed to create instance.cfg.");
			e.printStackTrace();
		}
	}

	public void writeManifest(ModPack pack) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(new File(pack.folderPath + File.separator + "manifest.json")), "UTF-8");

			String json = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(pack);

			writer.write(json);
			writer.close();

			System.out.println("Successfully wrote manifest.json");
		} catch (Exception e) {
			System.err.println("Failed to write manifest.json");
			e.printStackTrace();
		}
	}

	public int removeMods(ModPack oldPack, ModPack newPack) {
		int removed = 0;

		// Compare new mods to old mods and remove the same ones
		for (Mod newMod : newPack.files) {
			if (oldPack.modContained(newMod.projectID)) {

				Mod oldMod = oldPack.getMod(newMod.projectID);

				// If they aren't the same file then remove the old one
				// Make sure the old mod succeeded
				if (oldMod.success && newMod.fileID != oldMod.fileID) {
					File oldFile = new File(newPack.folderPath + File.separator + "minecraft" + File.separator + "mods" + File.separator + oldMod.fileName);

					if (oldFile.exists()) {
						if (oldFile.delete()) {
							removed++;
						} else {
							System.err.println("Failed to delete file at " + oldFile.getAbsolutePath());
						}
					} else {
						System.err.println(oldMod.fileName + " could not be found at " + oldFile.getParent());
					}
				}
			}
		}

		// Compare old mods to new mods and remove the missing ones
		for (Mod oldMod : oldPack.files) {

			// Check to see if the update no longer checks the mod contained
			if (!newPack.modContained(oldMod.projectID)) {
				if (oldMod.success) {
					File oldFile = new File(newPack.folderPath + File.separator + "minecraft" + File.separator + "mods" + File.separator + oldMod.fileName);

					if (oldFile.exists()) {
						if (oldFile.delete()) {
							removed++;
						} else {
							System.err.println("Failed to delete file at " + oldFile.getAbsolutePath());
						}
					} else {
						System.err.println(oldMod.fileName + " could not be found at " + oldFile.getParent());
					}
				}
			}
		}

		System.out.println("Removed " + removed + " old mods!");
		return removed;
	}

	public int removeOverrides(ModPack oldPack) {
		try {
			if (!oldPack.overrideFiles.isEmpty()) {
				int removed = 0;

				for (String override : oldPack.overrideFiles) {
					File path = new File(oldPack.folderPath + File.separator + "minecraft" + File.separator + override);

					if (path.exists()) {
						if (path.delete()) {
							removed++;
						} else {
							System.err.println("Failed to delete file at " + path.getAbsolutePath());
						}
					} else {
						System.err.println("Override could not be found at " + path.getAbsolutePath());
					}
				}

				System.out.println(removed + " overrides were removed!");
				return removed;
			} else {
				System.out.println("No override files found to be removed!");
			}
		} catch (Exception e) {
			Alerts.createErrorAlert("Error while removing overrides", "Error while removing overrides", e.getLocalizedMessage());
		}

		return 0;
	}
}
