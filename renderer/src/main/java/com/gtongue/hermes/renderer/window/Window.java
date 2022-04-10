package com.gtongue.hermes.renderer.window;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.gtongue.hermes.core.autoconfigure.HermesConfigService;
import com.gtongue.hermes.di.injection.InjectableClass;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryUtil.NULL;

@InjectableClass
@Slf4j
public class Window {

    private long glfwWindow;

    private int width;
    private int height;
    private String title;

    private float r;
    private float g;
    private float b;

    public Window(HermesConfigService hermesConfigService) {
        this.width = hermesConfigService.getConfigurationValue("window.width", Integer.class);
        this.height = hermesConfigService.getConfigurationValue("window.height", Integer.class);
        this.r = .2f;
        this.g = .2f;
        this.b = .2f;

        this.title = hermesConfigService.getConfigurationValueWithDefault("window.title", String.class, "HELLO WORLD");
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("render-thread-%d").build()).execute(this::run);
    }

    //  This should probably not be here.
    private void init() {
        log.info("Starting window using LWJGL {}", Version.getVersion());
        //        Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set(); //TODO: Log using slf4j instead of system err
//      Init GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

//        Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
//        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

//        Create the window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create the GLFW Window");
        }
//
//        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePositionCallback);
//        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
//        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
//        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

//        Make the OpenGL Context Current
        glfwMakeContextCurrent(glfwWindow);
//        Enable v-sync
        glfwSwapInterval(1);
//        TODO: Should we really be doing this?
        stbi_set_flip_vertically_on_load(true);
//        Make the window visible
        glfwShowWindow(glfwWindow);

//        Make opengl useable by loading the bindings
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void run() {
        init();
        loop();

//        Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        System.exit(0);
    }

    private void loop() {
        while(!glfwWindowShouldClose(glfwWindow)) {
            glfwPollEvents();
            glClearColor(r, g, b, 1);
            glClear(GL_COLOR_BUFFER_BIT);
//          TODO something here...
            glfwSwapBuffers(glfwWindow);
        }
    }
}
