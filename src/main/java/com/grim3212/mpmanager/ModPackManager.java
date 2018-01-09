package com.grim3212.mpmanager;

import com.google.gson.Gson;
import com.grim3212.mpmanager.pages.PageConsole;
import com.grim3212.mpmanager.pages.PageHome;
import com.grim3212.mpmanager.util.Settings;
import com.grim3212.mpmanager.util.Util;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class ModPackManager extends Application {

	public static Settings settings;
	public static Gson gson = new Gson();
	public static PageHome home;
	public static PageConsole console;
	public static SingleSelectionModel<Tab> selectedTab;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			// Setup pages
			home = new PageHome(primaryStage);

			// Make sure we setup the console early
			console = new PageConsole(primaryStage);

			// Create settings and load them
			settings = new Settings();

			primaryStage.setTitle("ModPack Manager");

			// Set minimum ui values
			primaryStage.setMinHeight(300);
			primaryStage.setMinWidth(300);

			TabPane tabs = new TabPane();
			selectedTab = tabs.getSelectionModel();
			
			//Tab 0
			Tab homeTab = new Tab("Home");
			homeTab.setContent(home.setupPage());
			homeTab.setClosable(false);
			tabs.getTabs().add(homeTab);

			//Tab 1
			Tab consoleTab = new Tab("Console");
			consoleTab.setContent(console.setupPage());
			consoleTab.setClosable(false);
			tabs.getTabs().add(consoleTab);

			Scene modpackManagerScene = new Scene(tabs, 350, 450);
			modpackManagerScene.getStylesheets().add(Util.getResource("stylesheets/application.css"));

			// Setup scene and show it
			primaryStage.setScene(modpackManagerScene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}