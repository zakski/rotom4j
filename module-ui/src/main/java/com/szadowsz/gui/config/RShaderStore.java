package com.szadowsz.gui.config;

import com.szadowsz.ui.constants.GlobalReferences;
import processing.opengl.PShader;

import java.util.HashMap;
import java.util.Map;

/**
 * Location to hold all GUI Shaders
 */
public class RShaderStore {
    private static final Map<String, PShader> shaders = new HashMap<>();
    private static final String shaderFolder = "shaders/";

    private RShaderStore() {/*NOOP*/}

    /**
     * Retrieve the shader from the cache/file system
     *
     * @param path location of the shader
     * @return the retrieved shader
     */
    public static PShader getOrLoadShader(String path) { // TODO LazyGui
        String fullPath = shaderFolder + path;
        if(!shaders.containsKey(fullPath)) {
            shaders.put(fullPath, GlobalReferences.app.loadShader(fullPath));
        }
        return shaders.get(fullPath);
    }
}
