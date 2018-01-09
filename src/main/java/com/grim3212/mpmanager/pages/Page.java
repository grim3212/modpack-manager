package com.grim3212.mpmanager.pages;

import javafx.scene.Node;
import javafx.stage.Stage;

public abstract class Page {

	protected Stage parent;

	public Page(Stage parent) {
		this.parent = parent;
	}

	public abstract Node setupPage();

}
