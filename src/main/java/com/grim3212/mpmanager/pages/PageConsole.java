package com.grim3212.mpmanager.pages;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.output.TeeOutputStream;

import com.grim3212.mpmanager.util.Console;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class PageConsole extends Page {

	private TextArea text;

	public PageConsole(Stage parent) {
		super(parent);

		this.text = new TextArea();
		this.text.setWrapText(true);
		this.text.setEditable(false);
		this.text.prefWidthProperty().bind(parent.widthProperty().subtract(20));
		this.text.prefHeightProperty().bind(parent.heightProperty());

		// Scroll to bottom
		this.text.textProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				text.setScrollTop(Double.MAX_VALUE);
			}
		});

		Console console = new Console(this.text);
		// Get original out/err so we can duplicate
		FileOutputStream outFos = new FileOutputStream(FileDescriptor.out);
		FileOutputStream errFos = new FileOutputStream(FileDescriptor.err);

		// Create the tee's
		TeeOutputStream out = new TeeOutputStream(outFos, console);
		TeeOutputStream err = new TeeOutputStream(errFos, console);

		PrintStream outPs = new PrintStream(out, true);
		PrintStream errPs = new PrintStream(err, true);

		System.setOut(outPs);
		System.setErr(errPs);
	}

	@Override
	public Node setupPage() {
		ScrollPane container = new ScrollPane();
		container.setContent(this.text);
		return container;
	}

}
