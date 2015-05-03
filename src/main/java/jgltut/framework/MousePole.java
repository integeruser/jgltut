package jgltut.framework;

import jgltut.jglsdk.glm.Vec2;
import jgltut.jglsdk.glutil.MousePoles.MouseButtons;
import jgltut.jglsdk.glutil.MousePoles.MouseModifiers;
import jgltut.jglsdk.glutil.MousePoles.Pole;
import org.lwjgl.input.Keyboard;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class MousePole {
    public static void forwardMouseMotion(Pole forward, int x, int y) {
        forward.mouseMove(new Vec2(x, y));
    }

    public static void forwardMouseButton(Pole forwardPole, int button, boolean state, int x, int y) {
        MouseModifiers modifiers = calcModifiers();
        Vec2 mouseLoc = new Vec2(x, y);
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

    public static void forwardMouseWheel(Pole forward, int direction, int x, int y) {
        forward.mouseWheel(direction, calcModifiers(), new Vec2(x, y));
    }


    private static MouseModifiers calcModifiers() {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            return MouseModifiers.MM_KEY_SHIFT;
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
            return MouseModifiers.MM_KEY_CTRL;
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) return MouseModifiers.MM_KEY_ALT;
        return null;
    }
}