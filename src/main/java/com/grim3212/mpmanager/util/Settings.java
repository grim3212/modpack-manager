package com.grim3212.mpmanager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.grim3212.mpmanager.ModPackManager;
import com.grim3212.mpmanager.components.Alerts;

public class Settings {

	public static final File settings = new File(System.getProperty("user.home") + File.separator + "modpack_manager" + File.separator + "settings.json");

	public Setting instanceLocation;
	public Setting ignoreFailedDownloads;

	public Settings() {
		instanceLocation = new Setting("config.instanceLocation", System.getProperty("user.home") + File.separator + "modpack_manager" + File.separator + "instances");
		ignoreFailedDownloads = new Setting("config.ignoreFailedDownloads", true);
	}

	public static class Setting {
		private String name;
		private Object defaultValue;
		private Object currentValue;
		private Type type;

		public Setting(String name, String defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;

			this.type = Type.STRING;
			this.setupValue();
		}

		public Setting(String name, boolean defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;

			this.type = Type.BOOLEAN;
			this.setupValue();
		}

		public Setting(String name, int defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;

			this.type = Type.INT;
			this.setupValue();
		}

		public Setting(String name, float defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;

			this.type = Type.FLOAT;
			this.setupValue();
		}

		private void setupValue() {
			try {
				if (!settings.exists()) {
					// Try and make the directory
					settings.getParentFile().mkdirs();
					// Then try and create the file
					if (settings.createNewFile()) {

						// Doesn't exist
						// Create file
						Writer writer = new OutputStreamWriter(new FileOutputStream(settings), "UTF-8");
						writer.write("{ }");
						writer.close();
					}
				}

				JsonReader reader = new JsonReader(new FileReader(settings));
				JsonObject obj = ModPackManager.gson.fromJson(reader, JsonObject.class);

				if (obj.has(name)) {
					System.out.println("Settings contains " + name);

					// Retrieve name if it is already set
					if (this.type == Type.BOOLEAN) {
						this.currentValue = obj.get(name).getAsBoolean();
					} else if (this.type == Type.STRING) {
						this.currentValue = obj.get(name).getAsString();
					} else if (this.type == Type.INT) {
						this.currentValue = obj.get(name).getAsInt();
					} else if (this.type == Type.FLOAT) {
						this.currentValue = obj.get(name).getAsFloat();
					}

				} else {
					System.out.println("Settings doesn't contain " + name);

					this.currentValue = this.defaultValue;

					// Add the default value to the file
					if (this.type == Type.BOOLEAN) {
						obj.addProperty(name, this.getAsBoolean());
					} else if (this.type == Type.STRING) {
						obj.addProperty(name, this.getAsString());
					} else if (this.type == Type.INT) {
						obj.addProperty(name, this.getAsInt());
					} else if (this.type == Type.FLOAT) {
						obj.addProperty(name, this.getAsFloat());
					}

					// Get new Json
					String updated = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(obj);

					// Write our update values
					Writer writer = new OutputStreamWriter(new FileOutputStream(settings), "UTF-8");
					writer.write(updated);
					writer.close();
				}
			} catch (

			IOException e) {
				e.printStackTrace();
			}
		}

		public void set(Object o) {
			this.currentValue = o;

			try {
				JsonReader reader = new JsonReader(new FileReader(settings));
				JsonObject obj = ModPackManager.gson.fromJson(reader, JsonObject.class);

				if (obj.has(name)) {
					if (this.type == Type.BOOLEAN) {
						obj.addProperty(name, this.getAsBoolean());
					} else if (this.type == Type.STRING) {
						obj.addProperty(name, this.getAsString());
					} else if (this.type == Type.INT) {
						obj.addProperty(name, this.getAsInt());
					} else if (this.type == Type.FLOAT) {
						obj.addProperty(name, this.getAsFloat());
					} else {
						throw new NullPointerException("Type for setting " + this.name + " was null. This should not happen!");
					}

					// Get new Json
					String updated = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(obj);

					// Write our update values
					Writer writer = new OutputStreamWriter(new FileOutputStream(settings), "UTF-8");
					writer.write(updated);
					writer.close();
				} else {
					throw new JsonParseException(this.name + " does not exist in the settings file. It should always exist!");
				}

			} catch (Exception e) {
				Alerts.createErrorAlert("Failed to set config", "Failed to update " + this.name, e.getLocalizedMessage());
			}
		}

		public String getAsString() {
			return (String) this.currentValue;
		}

		public int getAsInt() {
			return (int) this.currentValue;
		}

		public float getAsFloat() {
			return (float) this.currentValue;
		}

		public boolean getAsBoolean() {
			return (boolean) this.currentValue;
		}
	}

	public enum Type {
		BOOLEAN, STRING, INT, FLOAT
	}
}
