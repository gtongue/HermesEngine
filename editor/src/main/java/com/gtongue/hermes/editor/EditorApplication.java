package com.gtongue.hermes.editor;

import com.gtongue.hermes.di.context.HermesApp;
import com.gtongue.hermes.di.context.HermesApplication;

@HermesApp
public class EditorApplication {
    public static void main(String[] args) throws Exception {
        HermesApplication.inititalize(EditorApplication.class, args);
    }
}
