package jgltut.jglsdk.glutil;

import jgltut.jglsdk.glm.*;

import static org.lwjgl.glfw.GLFW.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class MousePoles {
    public enum MouseButtons {
        MB_LEFT_BTN,
        MB_RIGHT_BTN,
        MB_MIDDLE_BTN
    }

    public enum MouseModifiers {
        MM_KEY_SHIFT,
        MM_KEY_CTRL,
        MM_KEY_ALT
    }

    ////////////////////////////////
    public static class ObjectData {
        public Vec3 position;
        public Quaternion orientation;

        public ObjectData(Vec3 position, Quaternion orientation) {
            this.position = position;
            this.orientation = orientation;
        }

        public ObjectData(ObjectData objectData) {
            position = new Vec3(objectData.position);
            orientation = new Quaternion(objectData.orientation);
        }
    }

    public static class ViewData {
        public Vec3 targetPos;
        public Quaternion orient;
        public float radius;
        public float degSpinRotation;

        public ViewData(Vec3 targetPos, Quaternion orient, float radius, float degSpinRotation) {
            this.targetPos = targetPos;
            this.orient = orient;
            this.radius = radius;
            this.degSpinRotation = degSpinRotation;
        }

        public ViewData(ViewData viewData) {
            targetPos = new Vec3(viewData.targetPos);
            orient = new Quaternion(viewData.orient);
            radius = viewData.radius;
            degSpinRotation = viewData.degSpinRotation;
        }
    }

    public static class ViewScale {
        public float minRadius;
        public float maxRadius;
        public float largeRadiusDelta;
        public float smallRadiusDelta;
        public float largePosOffset;
        public float smallPosOffset;
        public float rotationScale;

        public ViewScale(float minRadius, float maxRadius, float largeRadiusDelta, float smallRadiusDelta,
                         float largePosOffset, float smallPosOffset, float rotationScale) {
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.largeRadiusDelta = largeRadiusDelta;
            this.smallRadiusDelta = smallRadiusDelta;
            this.largePosOffset = largePosOffset;
            this.smallPosOffset = smallPosOffset;
            this.rotationScale = rotationScale;
        }
    }

    ////////////////////////////////
    public static abstract class Pole {
        public abstract void mouseMove(Vec2 vec2);

        public abstract void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vec2 position);

        public abstract void mouseWheel(int direction, MouseModifiers modifiers, Vec2 position);


        public abstract void charPress(int key, boolean isShiftPressed, float lastFrameDuration);
    }


    public static class ObjectPole extends Pole {
        private enum Axis {
            AXIS_X,
            AXIS_Y,
            AXIS_Z,
        }

        private enum RotateMode {
            RM_DUAL_AXIS,
            RM_BIAXIAL,
            RM_SPIN,
        }

        private ViewProvider viewProvider;

        private ObjectData objectData;
        private ObjectData initialObjectData;

        private float rotateScale;
        private MouseButtons actionButton;

        // Used when rotating.
        private RotateMode rotateMode;
        private boolean isDragging;

        private Vec2 prevMousePos;
        private Vec2 startDragMousePos;
        private Quaternion startDragOrient;

        ////////////////////////////////
        public ObjectPole(ObjectData initialData, float rotateScale, MouseButtons actionButton, ViewProvider lookAtViewProvider) {
            objectData = new ObjectData(initialData);
            initialObjectData = initialData;
            this.rotateScale = rotateScale;
            this.actionButton = actionButton;
            viewProvider = lookAtViewProvider;
        }

        ////////////////////////////////
        @Override
        public void mouseMove(Vec2 position) {
            if (isDragging) {
                Vec2 diff = Vec2.sub(position, prevMousePos);

                switch (rotateMode) {
                    case RM_DUAL_AXIS: {
                        Quaternion rot = calcRotationQuat(Axis.AXIS_Y.ordinal(), diff.x * rotateScale);
                        rot = Glm.normalize(calcRotationQuat(Axis.AXIS_X.ordinal(), diff.y * rotateScale).mul(rot));
                        rotateViewDegrees(rot);
                        break;
                    }

                    case RM_BIAXIAL: {
                        Vec2 initDiff = Vec2.sub(position, startDragMousePos);

                        Axis axis;
                        float degAngle;
                        if (Math.abs(initDiff.x) > Math.abs(initDiff.y)) {
                            axis = Axis.AXIS_Y;
                            degAngle = initDiff.x * rotateScale;
                        } else {
                            axis = Axis.AXIS_X;
                            degAngle = initDiff.y * rotateScale;
                        }

                        Quaternion rot = calcRotationQuat(axis.ordinal(), degAngle);
                        rotateViewDegrees(rot, true);
                        break;
                    }

                    case RM_SPIN: {
                        rotateViewDegrees(calcRotationQuat(Axis.AXIS_Z.ordinal(), -diff.x * rotateScale));
                        break;
                    }
                }

                prevMousePos = position;
            }
        }

        @Override
        public void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vec2 position) {
            if (isPressed) {
                // Ignore button presses when dragging.
                if (!isDragging) {
                    if (button == actionButton) {
                        if (modifiers == MouseModifiers.MM_KEY_ALT) {
                            rotateMode = RotateMode.RM_SPIN;
                        } else if (modifiers == MouseModifiers.MM_KEY_CTRL) {
                            rotateMode = RotateMode.RM_BIAXIAL;
                        } else {
                            rotateMode = RotateMode.RM_DUAL_AXIS;
                        }

                        prevMousePos = position;
                        startDragMousePos = position;
                        startDragOrient = objectData.orientation;

                        isDragging = true;
                    }
                }
            } else {
                // Ignore up buttons if not dragging.
                if (isDragging) {
                    if (button == actionButton) {
                        mouseMove(position);

                        isDragging = false;
                    }
                }
            }
        }

        @Override
        public void mouseWheel(int direction, MouseModifiers modifiers, Vec2 position) {
        }


        @Override
        public void charPress(int key, boolean isShiftPressed, float lastFrameDuration) {
        }

        ////////////////////////////////
        public void reset() {
            if (!isDragging) {
                objectData = new ObjectData(initialObjectData);
            }
        }


        public Mat4 calcMatrix() {
            Mat4 translateMat = new Mat4(1.0f);
            translateMat.setColumn(3, new Vec4(objectData.position, 1.0f));
            return translateMat.mul(Glm.mat4Cast(objectData.orientation));
        }

        ////////////////////////////////
        private void rotateWorldDegrees(Quaternion rot, boolean fromInitial) {
            if (!isDragging) {
                fromInitial = false;
            }
            objectData.orientation = Glm.normalize(Quaternion.mul(rot, fromInitial ? startDragOrient : objectData.orientation));
        }


        private void rotateViewDegrees(Quaternion rot) {
            rotateViewDegrees(rot, false);
        }

        private void rotateViewDegrees(Quaternion rot, boolean bFromInitial) {
            if (!isDragging) {
                bFromInitial = false;
            }

            if (viewProvider != null) {
                Quaternion viewQuat = Glm.quatCast(viewProvider.calcMatrix());
                Quaternion invViewQuat = Glm.conjugate(viewQuat);

                objectData.orientation = Glm.normalize(Quaternion.mul(Quaternion.mul(invViewQuat, rot), (viewQuat)).mul(bFromInitial ? startDragOrient : objectData.orientation));
            } else {
                rotateWorldDegrees(rot, bFromInitial);
            }
        }

        ////////////////////////////////
        private Vec3 axisVectors[] = new Vec3[]{
                new Vec3(1.0f, 0.0f, 0.0f),
                new Vec3(0.0f, 1.0f, 0.0f),
                new Vec3(0.0f, 0.0f, 1.0f),
        };


        private Quaternion calcRotationQuat(int axis, float degAngle) {
            return Glm.angleAxis(degAngle, axisVectors[axis]);
        }
    }


    public static abstract class ViewProvider extends Pole {
        public abstract Mat4 calcMatrix();
    }

    public static class ViewPole extends ViewProvider {
        private enum RotateMode {
            RM_DUAL_AXIS_ROTATE,
            RM_BIAXIAL_ROTATE,
            RM_XZ_AXIS_ROTATE,
            RM_Y_AXIS_ROTATE,
            RM_SPIN_VIEW_AXIS
        }

        private enum TargetOffsetDir {
            DIR_UP,
            DIR_DOWN,
            DIR_FORWARD,
            DIR_BACKWARD,
            DIR_RIGHT,
            DIR_LEFT
        }

        private ViewData currView;
        private ViewScale viewScale;

        private ViewData initialView;
        private MouseButtons actionButton;
        private boolean rightKeyboardCtrls;

        // Used when rotating.
        private boolean isDragging;
        private RotateMode rotateMode;

        private float degStarDragSpin;
        private Vec2 startDragMouseLoc;
        private Quaternion startDragOrient;

        private Vec3 offsets[] = {
                new Vec3(0.0f, 1.0f, 0.0f),
                new Vec3(0.0f, -1.0f, 0.0f),
                new Vec3(0.0f, 0.0f, -1.0f),
                new Vec3(0.0f, 0.0f, 1.0f),
                new Vec3(1.0f, 0.0f, 0.0f),
                new Vec3(-1.0f, 0.0f, 0.0f)
        };

        ////////////////////////////////
        public ViewPole(ViewData initialView, ViewScale viewScale) {
            this(initialView, viewScale, MouseButtons.MB_LEFT_BTN, false);
        }

        public ViewPole(ViewData initialView, ViewScale viewScale, MouseButtons actionButton) {
            this(initialView, viewScale, actionButton, false);
        }

        public ViewPole(ViewData initialView, ViewScale viewScale, MouseButtons actionButton, boolean rightKeyboardCtrls) {
            currView = new ViewData(initialView);
            this.viewScale = viewScale;
            this.initialView = initialView;
            this.actionButton = actionButton;
            this.rightKeyboardCtrls = rightKeyboardCtrls;
        }

        ////////////////////////////////
        @Override
        public void mouseMove(Vec2 position) {
            if (isDragging) {
                onDragRotate(position);
            }
        }

        @Override
        public void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vec2 position) {
            if (isPressed) {
                // Ignore all other button presses when dragging.
                if (!isDragging) {
                    if (button == actionButton) {
                        if (modifiers == MouseModifiers.MM_KEY_CTRL) {
                            beginDragRotate(position, RotateMode.RM_BIAXIAL_ROTATE);
                        } else if (modifiers == MouseModifiers.MM_KEY_ALT) {
                            beginDragRotate(position, RotateMode.RM_SPIN_VIEW_AXIS);
                        } else {
                            beginDragRotate(position, RotateMode.RM_DUAL_AXIS_ROTATE);
                        }
                    }
                }
            } else {
                // Ignore all other button releases when not dragging
                if (isDragging) {
                    if (button == actionButton) {
                        if (rotateMode == RotateMode.RM_DUAL_AXIS_ROTATE || rotateMode == RotateMode.RM_SPIN_VIEW_AXIS
                                || rotateMode == RotateMode.RM_BIAXIAL_ROTATE) {
                            endDragRotate(position);
                        }
                    }
                }
            }
        }

        @Override
        public void mouseWheel(int direction, MouseModifiers modifiers, Vec2 position) {
            if (direction > 0) {
                moveCloser(modifiers != MouseModifiers.MM_KEY_SHIFT);
            } else {
                moveAway(modifiers != MouseModifiers.MM_KEY_SHIFT);
            }
        }


        @Override
        public void charPress(int key, boolean isShiftPressed, float lastFrameDuration) {
            if (rightKeyboardCtrls) {
                switch (key) {
                    case GLFW_KEY_I:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_FORWARD,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_K:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_BACKWARD,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_L:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_RIGHT,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_J:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_LEFT,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_O:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_UP,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_U:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_DOWN,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;
                }
            } else {
                switch (key) {
                    case GLFW_KEY_W:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_FORWARD,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_S:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_BACKWARD,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_D:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_RIGHT,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_A:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_LEFT,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_E:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_UP,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;

                    case GLFW_KEY_Q:
                        offsetTargetPos(
                                TargetOffsetDir.DIR_DOWN,
                                isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset,
                                lastFrameDuration);
                        break;
                }
            }
        }


        @Override
        public Mat4 calcMatrix() {
            Mat4 mat = new Mat4(1.0f);

            // Remember: these transforms are in reverse order.

            // In this space, we are facing in the correct direction. Which means that the camera point
            // is directly behind us by the radius number of units.
            mat = Glm.translate(mat, new Vec3(0.0f, 0.0f, -currView.radius));

            // Rotate the world to look in the right direction.
            Quaternion fullRotation = Glm.angleAxis(currView.degSpinRotation, new Vec3(0.0f, 0.0f, 1.0f)).mul(currView.orient);

            mat.mul(Glm.mat4Cast(fullRotation));

            // Translate the world by the negation of the lookat point, placing the origin at the lookat point.
            mat = Glm.translate(mat, Vec3.negate(currView.targetPos));
            return mat;
        }

        ////////////////////////////////
        public void reset() {
            if (!isDragging) {
                currView = new ViewData(initialView);
            }
        }


        public ViewData getView() {
            return currView;
        }

        ////////////////////////////////
        private void beginDragRotate(Vec2 ptStart, RotateMode rotMode) {
            rotateMode = rotMode;
            startDragMouseLoc = ptStart;
            degStarDragSpin = currView.degSpinRotation;
            startDragOrient = currView.orient;
            isDragging = true;
        }


        private void onDragRotate(Vec2 ptCurr) {
            Vec2 diff = Vec2.sub(ptCurr, startDragMouseLoc);
            int diffX = (int) diff.x;
            int diffY = (int) diff.y;

            switch (rotateMode) {
                case RM_DUAL_AXIS_ROTATE:
                    processXYChange(diffX, diffY);
                    break;

                case RM_BIAXIAL_ROTATE:
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        processXChange(diffX, true);
                    } else {
                        processYChange(diffY, true);
                    }
                    break;

                case RM_XZ_AXIS_ROTATE:
                    processXChange(diffX);
                    break;

                case RM_Y_AXIS_ROTATE:
                    processYChange(diffY);
                    break;

                case RM_SPIN_VIEW_AXIS:
                    processSpinAxis(diffX, diffY);
                    break;
            }
        }


        private void endDragRotate(Vec2 ptEnd) {
            endDragRotate(ptEnd, true);
        }

        private void endDragRotate(Vec2 ptEnd, boolean keepResults) {
            if (keepResults) {
                onDragRotate(ptEnd);
            } else {
                currView.orient = startDragOrient;
            }

            isDragging = false;
        }


        private void processXChange(int diffX) {
            processXChange(diffX, false);
        }

        private void processXChange(int diffX, boolean clearY) {
            float degAngleDiff = diffX * viewScale.rotationScale;

            // Rotate about the world-space Y axis.
            currView.orient = Quaternion.mul(startDragOrient, Glm.angleAxis(degAngleDiff, new Vec3(0.0f, 1.0f, 0.0f)));
        }


        private void processYChange(int diffY) {
            processYChange(diffY, false);
        }

        private void processYChange(int diffY, boolean clearXZ) {
            float degAngleDiff = diffY * viewScale.rotationScale;

            // Rotate about the local-space X axis.
            currView.orient = Glm.angleAxis(degAngleDiff, new Vec3(1.0f, 0.0f, 0.0f)).mul(startDragOrient);
        }


        private void processXYChange(int diffX, int diffY) {
            float degXAngleDiff = (diffX * viewScale.rotationScale);
            float degYAngleDiff = (diffY * viewScale.rotationScale);

            // Rotate about the world-space Y axis.
            currView.orient = Quaternion.mul(startDragOrient, Glm.angleAxis(degXAngleDiff, new Vec3(0.0f, 1.0f, 0.0f)));
            // Rotate about the local-space X axis.
            currView.orient = Glm.angleAxis(degYAngleDiff, new Vec3(1.0f, 0.0f, 0.0f)).mul(currView.orient);
        }


        private void processSpinAxis(int diffX, int diffY) {
            float degSpinDiff = diffX * viewScale.rotationScale;
            currView.degSpinRotation = degSpinDiff + degStarDragSpin;
        }


        private void moveCloser(boolean largeStep) {
            if (largeStep) {
                currView.radius -= viewScale.largeRadiusDelta;
            } else {
                currView.radius -= viewScale.smallRadiusDelta;
            }

            if (currView.radius < viewScale.minRadius) {
                currView.radius = viewScale.minRadius;
            }
        }


        private void moveAway(boolean largeStep) {
            if (largeStep) {
                currView.radius += viewScale.largeRadiusDelta;
            } else {
                currView.radius += viewScale.smallRadiusDelta;
            }

            if (currView.radius > viewScale.maxRadius) {
                currView.radius = viewScale.maxRadius;
            }
        }


        private void offsetTargetPos(TargetOffsetDir dir, float worldDistance, float lastFrameDuration) {
            Vec3 offsetDir = new Vec3(offsets[dir.ordinal()]);
            offsetTargetPos(offsetDir.scale(worldDistance).scale(lastFrameDuration), lastFrameDuration);
        }

        private void offsetTargetPos(Vec3 cameraOffset, float lastFrameDuration) {
            Mat4 currMat = calcMatrix();
            Quaternion orientation = Glm.quatCast(currMat);

            Quaternion invOrient = Glm.conjugate(orientation);
            Vec3 worldOffset = invOrient.mul(cameraOffset);

            currView.targetPos.add(worldOffset);
        }
    }
}