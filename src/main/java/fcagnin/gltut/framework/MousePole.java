package fcagnin.gltut.framework;

import org.lwjgl.input.Keyboard;

import fcagnin.jglsdk.glm.Vec2;
import fcagnin.jglsdk.glutil.MousePoles.MouseButtons;
import fcagnin.jglsdk.glutil.MousePoles.MouseModifiers;
import fcagnin.jglsdk.glutil.MousePoles.Pole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
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
	
	
	public static void forwardMouseWheel(Pole forward, int wheel, int direction, int x, int y) {
		forward.mouseWheel(direction, calcModifiers(), new Vec2(x, y));
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static MouseModifiers calcModifiers() {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			return MouseModifiers.MM_KEY_SHIFT;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
			return MouseModifiers.MM_KEY_CTRL;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
			return MouseModifiers.MM_KEY_ALT;
		}
		
		return null;
	}
}