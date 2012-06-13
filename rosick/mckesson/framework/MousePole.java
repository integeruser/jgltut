package rosick.mckesson.framework;

import org.lwjgl.input.Keyboard;

import rosick.jglsdk.glm.Vec2;
import rosick.jglsdk.glutil.MousePoles.MouseButtons;
import rosick.jglsdk.glutil.MousePoles.MouseModifiers;
import rosick.jglsdk.glutil.MousePoles.Pole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class MousePole {

	public static void forwardMouseMotion(Pole forward, int x, int y) {
		forward.mouseMove(new Vec2(x, y));		
	}
	
	
	public static void forwardMouseButton(Pole forward, int button, boolean state, int x, int y) {
		MouseModifiers modifiers = calc_glut_modifiers();
		Vec2 mouseLoc = new Vec2(x, y);
		MouseButtons eButton = null;

		switch (button) {
			case 0:
				eButton = MouseButtons.MB_LEFT_BTN;
				break;
			case 1:
				eButton = MouseButtons.MB_RIGHT_BTN;
				break;
			case 2:
				eButton = MouseButtons.MB_MIDDLE_BTN;
				break;
		}

		forward.mouseClick(eButton, state, modifiers, mouseLoc);
	}
	
	
	public static void forwardMouseWheel(Pole forward, int wheel, int direction, int x, int y) {
		forward.mouseWheel(direction, calc_glut_modifiers(), new Vec2(x, y));
	}
	
	
	private static MouseModifiers calc_glut_modifiers() {		
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