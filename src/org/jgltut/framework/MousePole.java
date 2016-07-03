package org.jgltut.framework;

import org.jgltut.jglsdk.glutil.MousePoles.MouseButtons;
import org.jgltut.jglsdk.glutil.MousePoles.MouseModifiers;
import org.jgltut.jglsdk.glutil.MousePoles.Pole;
import org.joml.Vector2i;

import static org.lwjgl.glfw.GLFW.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class MousePole {
    public static void forwardMouseMotion(Pole forward, int x, int y) {
        forward.mouseMove(new Vector2i(x, y));
    }

    public static void forwardMouseButton(long window, Pole forwardPole, int button, boolean state, int x, int y) {
        MouseModifiers modifiers = calcModifiers(window);
        Vector2i mouseLoc = new Vector2i(x, y);
        MouseButtons mouseButtons = null;

        switch (button) {
            case 0:
                mouseButtons = MouseButtons.MB_LEFT_BTN;
                break;

            case 1:
                mouseButtons = MouseButtons.MB_RIGHT_BTN;
                break;

            case 2:
                mouseButtons = MouseButtons.MB_MIDDLE_BTN;
                break;
        }

        forwardPole.mouseClick(mouseButtons, state, modifiers, mouseLoc);
    }

    public static void forwardMouseWheel(long window, Pole forward, int direction, int x, int y) {
        forward.mouseWheel(direction, calcModifiers(window), new Vector2i(x, y));
    }


    private static MouseModifiers calcModifiers(long window) {
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == 1 || glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) == 1)
            return MouseModifiers.MM_KEY_SHIFT;
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == 1 || glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == 1)
            return MouseModifiers.MM_KEY_CTRL;
        if (glfwGetKey(window, GLFW_KEY_MENU) == 1)
            return MouseModifiers.MM_KEY_ALT;
        return null;
    }
}