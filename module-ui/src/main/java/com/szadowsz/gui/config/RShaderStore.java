package com.szadowsz.gui.config;

import com.szadowsz.gui.RotomGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.opengl.PShader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Location to hold all GUI Shaders
 */
public class RShaderStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(RShaderStore.class);
    private static final Map<String, PShader> shaders = new HashMap<>();
    private static final String shaderFolder = "./data/shaders/";

    private RShaderStore() {/*NOOP*/}

    /**
     * Retrieve the shader from the cache/file system
     *
     * @param path location of the shader
     * @return the retrieved shader
     */
    public static PShader getOrLoadShader(RotomGui gui, String path) { // TODO LazyGui
        String fullPath = shaderFolder + path;
        if(!shaders.containsKey(fullPath)) {
            File dir = new File(fullPath);
            LOGGER.info("Loading shader " + dir.getAbsolutePath());
            shaders.put(fullPath, gui.getSketch().loadShader(fullPath));
        }
        return shaders.get(fullPath);
    }
}
