package com.grim3212.mpmanager.pages;

import java.io.File;

import com.grim3212.mpmanager.ModPackManager;
import com.grim3212.mpmanager.components.Alerts;
import com.grim3212.mpmanager.components.ModPackDownload;
import com.grim3212.mpmanager.components.PersistentTextField;
import com.grim3212.mpmanager.json.ModPack;
import com.grim3212.mpmanager.util.Util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PageHome {

	private Stage parent;
	private FlowPane modPacksContainer;

	public PageHome(Stage parent) {
		this.parent = parent;
	}

	public Scene setupPage() {
		BorderPane container = new BorderPane();
		container.setPadding(new Insets(25, 25, 25, 25));

		HBox topBar = new HBox(15);
		topBar.setPadding(new Insets(0, 0, 35, 0));
		topBar.setAlignment(Pos.TOP_CENTER);
		container.setTop(topBar);

		Button addInstanceBtn = new Button("Add Instance");
		addInstanceBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				addInstance();
			}
		});
		topBar.getChildren().add(addInstanceBtn);

		Button instanceFolderBtn = new Button("Instance Folder");
		instanceFolderBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setInstanceLocation();
			}
		});
		topBar.getChildren().add(instanceFolderBtn);

		modPacksContainer = new FlowPane();
		modPacksContainer.setVgap(4);
		modPacksContainer.setHgap(4);
		modPacksContainer.setPadding(new Insets(4, 4, 12, 4));
		container.setCenter(modPacksContainer);

		this.refreshModPacks();

		Scene homeScene = new Scene(container, 350, 450);
		homeScene.getStylesheets().add(Util.getResource("stylesheets/application.css"));

		return homeScene;
	}

	public void refreshModPacks() {
		modPacksContainer.getChildren().clear();

		for (ModPack pack : Util.getCurrentPacks()) {
			VBox mp = new VBox();
			Text modPackName = new Text(pack.givenName);
			modPackName.setFont(Font.font("Verdana", 20));
			Button updateBtn = new Button("Update Pack");
			updateBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					updateModPack(pack);
				}
			});
			mp.getChildren().add(modPackName);
			mp.getChildren().add(updateBtn);

			modPacksContainer.getChildren().add(mp);
		}
	}

	public void addInstance() {
		Stage dialog = new Stage();

		dialog.initOwner(this.parent);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Add a modpack");
		dialog.setResizable(false);

		BorderPane border = new BorderPane();
		border.setPadding(new Insets(25, 25, 25, 25));

		GridPane container = new GridPane();
		container.setAlignment(Pos.TOP_CENTER);
		container.setHgap(5);
		container.setVgap(5);

		Label nameLbl = new Label("Name: ");
		PersistentTextField nameField = new PersistentTextField("", "ModPack Name");
		container.add(nameLbl, 0, 0);
		container.add(nameField, 1, 0);

		Label urlLbl = new Label("URL: ");
		HBox urlBox = new HBox();
		PersistentTextField urlField = new PersistentTextField("", "ModPack Location");
		Button fileBtn = new Button("...");

		fileBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Open up a file browser
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Accepted File Types", "*.zip"), new FileChooser.ExtensionFilter("All Files", "*.*"));

				fileChooser.setTitle("Choose your modpack zip...");
				File file = fileChooser.showOpenDialog(dialog);

				if (file != null) {

					// Tell the user about only accepting zips
					if (!file.getAbsolutePath().endsWith(".zip")) {
						Alerts.createErrorAlert("Wrong modpack type selected", "Only zip files are allowed", "A zip file must be the end result of the location either online or local.");
					} else {
						urlField.setText(file.getAbsolutePath());
					}
				}
			}
		});

		urlBox.getChildren().add(urlField);
		urlBox.getChildren().add(fileBtn);
		container.add(urlLbl, 0, 1);
		container.add(urlBox, 1, 1);

		border.setCenter(container);

		HBox finishBox = new HBox();
		finishBox.setAlignment(Pos.CENTER);

		Button confirmBtn = new Button("Confirm");
		confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!nameField.getText().isEmpty()) {

					if (!urlField.getText().isEmpty()) {
						File folder = new File(ModPackManager.settings.instanceLocation.getAsString() + File.separator + nameField.getText());

						if (!folder.exists()) {
							// Close the dialog
							dialog.close();

							// If both are good then pass the data and start processing
							new ModPackDownload(parent, nameField.getText(), urlField.getText()).startDownload();
						} else {
							// Close the dialog
							Alerts.createErrorAlert("Folder already exists", "Instance already exists", "An instance folder already exists at " + folder.getAbsolutePath() + ". Please either delete this folder or choose a different name");
						}
					} else {
						Alerts.createErrorAlert("Url Field is Empty", "Missing url!", "Modpack location must contain either a link that contains a valid zip with a manifest.json or a zip file on your computer with a valid manifest.json.");
					}
				} else {
					Alerts.createErrorAlert("Name Field is Empty", "Missing name!", "You must set the name of the modpack. This will serve as the folder name for it.");
				}
			}
		});

		Button cancelBtn = new Button("Cancel");
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Cancel and close the dialog
				dialog.close();
			}
		});

		finishBox.getChildren().addAll(confirmBtn, cancelBtn);
		border.setBottom(finishBox);

		Scene addInstanceScene = new Scene(border, 300, 145);
		addInstanceScene.getStylesheets().add(Util.getResource("stylesheets/application.css"));

		// Add everything to the dialog and display it
		dialog.setScene(addInstanceScene);
		dialog.showAndWait();
	}

	public void setInstanceLocation() {
		Stage dialog = new Stage();

		dialog.initOwner(this.parent);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Choose instance location");
		dialog.setResizable(false);

		BorderPane border = new BorderPane();
		border.setPadding(new Insets(25, 25, 25, 25));

		GridPane container = new GridPane();
		container.setAlignment(Pos.TOP_CENTER);
		container.setHgap(5);
		container.setVgap(5);

		Label urlLbl = new Label("Location: ");
		HBox urlBox = new HBox();
		PersistentTextField urlField = new PersistentTextField("", "Instance Location");
		Button fileBtn = new Button("...");

		fileBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Open up a file browser
				DirectoryChooser dirChooser = new DirectoryChooser();

				dirChooser.setTitle("Choose a new instance folder...");
				File file = dirChooser.showDialog(dialog);

				if (file != null) {
					urlField.setText(file.getAbsolutePath());
				}
			}
		});

		urlBox.getChildren().add(urlField);
		urlBox.getChildren().add(fileBtn);
		container.add(urlLbl, 0, 0);
		container.add(urlBox, 1, 0);

		border.setCenter(container);

		HBox finishBox = new HBox();
		finishBox.setAlignment(Pos.CENTER);

		Button confirmBtn = new Button("Confirm");
		confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if (!urlField.getText().isEmpty()) {
					// If the url is good then set the instance folder
					ModPackManager.settings.instanceLocation.set(urlField.getText());

					// Update modpacks list
					refreshModPacks();
				} else {
					Alerts.createErrorAlert("Instance Location is empty", "Missing location!", "The instance location is empty. This must be somewhere as this is where modpacks will download.");
				}

				// Close the dialog
				dialog.close();
			}
		});

		Button cancelBtn = new Button("Cancel");
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Cancel and close the dialog
				dialog.close();
			}
		});

		finishBox.getChildren().addAll(confirmBtn, cancelBtn);
		border.setBottom(finishBox);

		Scene addInstanceScene = new Scene(border, 300, 145);
		addInstanceScene.getStylesheets().add(Util.getResource("stylesheets/application.css"));

		// Add everything to the dialog and display it
		dialog.setScene(addInstanceScene);
		dialog.showAndWait();
	}

	public void updateModPack(ModPack pack) {
		Stage dialog = new Stage();

		dialog.initOwner(this.parent);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Update " + pack.name);
		dialog.setResizable(false);

		BorderPane border = new BorderPane();
		border.setPadding(new Insets(25, 25, 25, 25));

		GridPane container = new GridPane();
		container.setAlignment(Pos.TOP_CENTER);
		container.setHgap(5);
		container.setVgap(5);

		Label urlLbl = new Label("URL: ");
		HBox urlBox = new HBox();
		PersistentTextField urlField = new PersistentTextField("", "Update url");
		Button fileBtn = new Button("...");

		fileBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Open up a file browser
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Accepted File Types", "*.zip"), new FileChooser.ExtensionFilter("All Files", "*.*"));

				fileChooser.setTitle("Choose your update zip...");
				File file = fileChooser.showOpenDialog(dialog);

				if (file != null) {
					// Tell the user about only accepting zips
					if (!file.getAbsolutePath().endsWith(".zip")) {
						Alerts.createErrorAlert("Wrong modpack type selected", "Only zip files are allowed", "A zip file must be the end result of the location either online or local.");
					} else {
						urlField.setText(file.getAbsolutePath());
					}
				}
			}
		});

		urlBox.getChildren().add(urlField);
		urlBox.getChildren().add(fileBtn);
		container.add(urlLbl, 0, 0);
		container.add(urlBox, 1, 0);

		border.setCenter(container);

		HBox finishBox = new HBox();
		finishBox.setAlignment(Pos.CENTER);

		Button confirmBtn = new Button("Confirm");
		confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if (!urlField.getText().isEmpty()) {
					// Close the dialog
					dialog.close();

					// If link is good then try to update pack
					new ModPackDownload(parent, pack, urlField.getText()).startDownload();
				} else {
					Alerts.createErrorAlert("Instance Location is empty", "Missing location!", "The instance location is empty. This must be somewhere as this is where modpacks will download.");
				}

				// Close the dialog
				dialog.close();
			}
		});

		Button cancelBtn = new Button("Cancel");
		cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Cancel and close the dialog
				dialog.close();
			}
		});

		finishBox.getChildren().addAll(confirmBtn, cancelBtn);
		border.setBottom(finishBox);

		Scene addInstanceScene = new Scene(border, 300, 145);
		addInstanceScene.getStylesheets().add(Util.getResource("stylesheets/application.css"));

		// Add everything to the dialog and display it
		dialog.setScene(addInstanceScene);
		dialog.showAndWait();
	}
}
