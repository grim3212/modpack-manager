package com.grim3212.mpmanager.components;

import javafx.scene.control.TextField;

public class PersistentTextField extends TextField {
	
    public PersistentTextField(String text, String prompt) {
        super(text);
        setPromptText(prompt);
        getStyleClass().add("persistent-prompt");
        refreshPromptVisibility();

        textProperty().addListener(observable -> refreshPromptVisibility());
    }

    private void refreshPromptVisibility() {
        final String text = getText();
        if (isEmptyString(text)) {
            getStyleClass().remove("no-prompt");
        } else {
            if (!getStyleClass().contains("no-prompt")) {
                getStyleClass().add("no-prompt");
            }
        }
    }

    private boolean isEmptyString(String text) {
        return text == null || text.isEmpty();
    }
}
