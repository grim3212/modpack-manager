package com.grim3212.mpmanager.components;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Alerts {
	public static void createErrorAlert(String title, String header, String err) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);

		if (!(header == null || header.isEmpty()))
			alert.setHeaderText(header);
		alert.setContentText(err);

		alert.showAndWait();
	}
}
