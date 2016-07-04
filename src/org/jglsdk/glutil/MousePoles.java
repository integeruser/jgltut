package org.jglsdk.glutil;

import org.jglsdk.glm.Glm;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
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
        public Vector3f position;
        public Quaternionf orientation;

        public ObjectData(Vector3f position, Quaternionf orientation) {
            this.position = position;
            this.orientation = orientation;
        }

        public ObjectData(ObjectData objectData) {
            position = new Vector3f(objectData.position);
            orientation = new Quaternionf(objectData.orientation);
        }
    }

    public static class ViewData {
        public Vector3f targetPos;
        public Quaternionf orient;
        public float radius;
        public float degSpinRotation;

        public ViewData(Vector3f targetPos, Quaternionf orient, float radius, float degSpinRotation) {
            this.targetPos = targetPos;
            this.orient = orient;
            this.radius = radius;
            this.degSpinRotation = degSpinRotation;
        }

        public ViewData(ViewData viewData) {
            targetPos = new Vector3f(viewData.targetPos);
            orient = new Quaternionf(viewData.orient);
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
        public abstract void mouseMove(Vector2i vec2);

        public abstract void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vector2i position);

        public abstract void mouseWheel(int direction, MouseModifiers modifiers, Vector2i position);


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

        private Vector2i prevMousePos;
        private Vector2i startDragMousePos;
        private Quaternionf startDragOrient;

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
        public void mouseMove(Vector2i position) {
            if (isDragging) {
                Vector2i diff = new Vector2i(position).sub(prevMousePos);

                switch (rotateMode) {
                    case RM_DUAL_AXIS: {
                        Quaternionf rot = calcRotationQuat(Axis.AXIS_Y.ordinal(), diff.x * rotateScale);
                        rot = calcRotationQuat(Axis.AXIS_X.ordinal(), diff.y * rotateScale).mul(rot).normalize();
                        rotateViewDegrees(rot);
                        break;
                    }

                    case RM_BIAXIAL: {
                        Vector2i initDiff = new Vector2i(position).sub(startDragMousePos);

                        Axis axis;
                        float degAngle;
                        if (Math.abs(initDiff.x) > Math.abs(initDiff.y)) {
                            axis = Axis.AXIS_Y;
                            degAngle = initDiff.x * rotateScale;
                        } else {
                            axis = Axis.AXIS_X;
                            degAngle = initDiff.y * rotateScale;
                        }

                        Quaternionf rot = calcRotationQuat(axis.ordinal(), degAngle);
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
        public void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vector2i position) {
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
        public void mouseWheel(int direction, MouseModifiers modifiers, Vector2i position) {
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


        public Matrix4f calcMatrix() {
            Matrix4f translateMat = new Matrix4f();
            translateMat.m30(objectData.position.x);
            translateMat.m31(objectData.position.y);
            translateMat.m32(objectData.position.z);
            return translateMat.mul(objectData.orientation.get(new Matrix4f()));
        }

        ////////////////////////////////
        private void rotateWorldDegrees(Quaternionf rot, boolean fromInitial) {
            if (!isDragging) {
                fromInitial = false;
            }
            objectData.orientation = new Quaternionf(rot).mul(fromInitial ? startDragOrient : objectData.orientation).normalize();
        }


        private void rotateViewDegrees(Quaternionf rot) {
            rotateViewDegrees(rot, false);
        }

        private void rotateViewDegrees(Quaternionf rot, boolean bFromInitial) {
            if (!isDragging) {
                bFromInitial = false;
            }

            if (viewProvider != null) {
                Quaternionf viewQuat = new Quaternionf();
                viewProvider.calcMatrix().getNormalizedRotation(viewQuat);
                Quaternionf invViewQuat = new Quaternionf(viewQuat).conjugate();

                Quaternionf tmp = new Quaternionf(invViewQuat).mul(rot);
                objectData.orientation = tmp.mul(viewQuat).mul(bFromInitial ? startDragOrient : objectData.orientation);
                objectData.orientation.normalize();
            } else {
                rotateWorldDegrees(rot, bFromInitial);
            }
        }

        ////////////////////////////////
        private Vector3f axisVectors[] = new Vector3f[]{
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 1.0f),
        };


        private Quaternionf calcRotationQuat(int axis, float degAngle) {
            return new Quaternionf().setAngleAxis((float) Math.toRadians(degAngle), axisVectors[axis].x, axisVectors[axis].y, axisVectors[axis].z);
        }
    }


    public static abstract class ViewProvider extends Pole {
        public abstract Matrix4f calcMatrix();
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
        private Vector2i startDragMouseLoc;
        private Quaternionf startDragOrient;

        private Vector3f offsets[] = {
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector3f(0.0f, -1.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, -1.0f),
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(-1.0f, 0.0f, 0.0f)
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
        public void mouseMove(Vector2i position) {
            if (isDragging) {
                onDragRotate(position);
            }
        }

        @Override
        public void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vector2i position) {
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
        public void mouseWheel(int direction, MouseModifiers modifiers, Vector2i position) {
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
        public Matrix4f calcMatrix() {
            Matrix4f mat = new Matrix4f();

            // Remember: these transforms are in reverse order.

            // In this space, we are facing in the correct direction. Which means that the camera point
            // is directly behind us by the radius number of units.
            mat.translate(new Vector3f(0.0f, 0.0f, -currView.radius));

            // Rotate the world to look in the right direction.
            Quaternionf angleAxis = new Quaternionf().setAngleAxis((float) Math.toRadians(currView.degSpinRotation), 0.0f, 0.0f, 1.0f);
            Quaternionf fullRotation = angleAxis.mul(currView.orient);

            mat.mul(fullRotation.get(new Matrix4f()));

            // Translate the world by the negation of the lookat point, placing the origin at the lookat point.
            mat.translate(new Vector3f(currView.targetPos).negate());
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
        private void beginDragRotate(Vector2i ptStart, RotateMode rotMode) {
            rotateMode = rotMode;
            startDragMouseLoc = ptStart;
            degStarDragSpin = currView.degSpinRotation;
            startDragOrient = currView.orient;
            isDragging = true;
        }


        private void onDragRotate(Vector2i ptCurr) {
            Vector2i diff = new Vector2i(ptCurr).sub(startDragMouseLoc);
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


        private void endDragRotate(Vector2i ptEnd) {
            endDragRotate(ptEnd, true);
        }

        private void endDragRotate(Vector2i ptEnd, boolean keepResults) {
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
            Quaternionf angleAxisY = new Quaternionf().setAngleAxis((float) Math.toRadians(degAngleDiff), 0.0f, 1.0f, 0.0f);
            currView.orient = new Quaternionf(startDragOrient).mul(angleAxisY);
        }


        private void processYChange(int diffY) {
            processYChange(diffY, false);
        }

        private void processYChange(int diffY, boolean clearXZ) {
            float degAngleDiff = diffY * viewScale.rotationScale;

            // Rotate about the local-space X axis.
            Quaternionf angleAxisX = new Quaternionf().setAngleAxis((float) Math.toRadians(degAngleDiff), 1.0f, 0.0f, 0.0f);
            currView.orient = angleAxisX.mul(startDragOrient);
        }


        private void processXYChange(int diffX, int diffY) {
            float degXAngleDiff = (diffX * viewScale.rotationScale);
            float degYAngleDiff = (diffY * viewScale.rotationScale);

            // Rotate about the world-space Y axis.
            Quaternionf angleAxisY = new Quaternionf().setAngleAxis((float) Math.toRadians(degXAngleDiff), 0.0f, 1.0f, 0.0f);
            currView.orient = new Quaternionf(startDragOrient).mul(angleAxisY);
            // Rotate about the local-space X axis.
            Quaternionf angleAxisX = new Quaternionf().setAngleAxis((float) Math.toRadians(degYAngleDiff), 1.0f, 0.0f, 0.0f);
            currView.orient = angleAxisX.mul(currView.orient);
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
            Vector3f offsetDir = new Vector3f(offsets[dir.ordinal()]);
            offsetTargetPos(offsetDir.mul(worldDistance).mul(lastFrameDuration), lastFrameDuration);
        }

        private void offsetTargetPos(Vector3f cameraOffset, float lastFrameDuration) {
            Matrix4f currMat = calcMatrix();
            Quaternionf orientation = new Quaternionf();
            currMat.getNormalizedRotation(orientation);

            Quaternionf invOrient = new Quaternionf(orientation).conjugate();
            Vector3f worldOffset = invOrient.transform(new Vector3f(cameraOffset));

            currView.targetPos.add(worldOffset);
        }
    }
}