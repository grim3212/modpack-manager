package com.grim3212.mpmanager;

import com.google.gson.Gson;
import com.grim3212.mpmanager.pages.PageHome;
import com.grim3212.mpmanager.util.Settings;

import javafx.application.Application;
import javafx.stage.Stage;

public class ModPackManager extends Application {

	public static Settings settings;
	public static Gson gson = new Gson();
	public static PageHome home;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			// Create settings and load them
			settings = new Settings();

			primaryStage.setTitle("ModPack Manager");

			// Set minimum ui values
			primaryStage.setMinHeight(300);
			primaryStage.setMinWidth(300);
			
			//Setup home page
			home = new PageHome(primaryStage);

			// Set and display homepage
			primaryStage.setScene(home.setupPage());
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}