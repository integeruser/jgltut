package integeruser.jgltut.framework;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/framework/Interpolators.h
 */
public class Interpolators {
    public static class WeightedLinearInterpolatorFloat {
        public ArrayList<Data> values = new ArrayList<>();

        public class Data {
            public float data;
            public float weight;
        }


        public float interpolate(float alpha) {
            if (values.isEmpty()) return 0.0f;

            if (values.size() == 1) return values.get(0).data;

            // Find which segment we are within.
            int segment = 1;
            for (; segment < values.size(); segment++) {
                if (alpha < values.get(segment).weight) break;
            }
            if (segment == values.size()) return values.get(segment - 1).data;

            float sectionAlpha = alpha - values.get(segment - 1).weight;
            sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;

            float invSecAlpha = 1.0f - sectionAlpha;
            return values.get(segment - 1).data * invSecAlpha + values.get(segment).data * sectionAlpha;
        }
    }


    public static class WeightedLinearInterpolatorVec3 {
        public ArrayList<Data> values = new ArrayList<>();

        public class Data {
            Vector3f data;
            float weight;
        }


        public Vector3f interpolate(float alpha) {
            if (values.isEmpty()) return new Vector3f();

            if (values.size() == 1) return new Vector3f(values.get(0).data);

            // Find which segment we are within.
            int segment = 1;
            for (; segment < values.size(); segment++) {
                if (alpha < values.get(segment).weight) break;
            }
            if (segment == values.size()) return new Vector3f(values.get(segment - 1).data);

            float sectionAlpha = alpha - values.get(segment - 1).weight;
            sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;

            float invSecAlpha = 1.0f - sectionAlpha;
            return new Vector3f(values.get(segment - 1).data).mul(invSecAlpha).add(new Vector3f(values.get(segment).data).mul(sectionAlpha));
        }
    }

    public static class ConstVelLinearInterpolatorVec3 extends WeightedLinearInterpolatorVec3 {
        float totalDist;

        public void setValues(ArrayList<Vector3f> data) {
            setValues(data, true);
        }

        public void setValues(ArrayList<Vector3f> data, boolean isLoop) {
            values.clear();

            for (Vector3f curr : data) {
                Data currData = new Data();
                currData.data = new Vector3f(curr);
                currData.weight = 0.0f;
                values.add(currData);
            }

            if (isLoop) {
                Data currData = new Data();
                currData.data = new Vector3f(data.get(0));
                currData.weight = 0.0f;
                values.add(currData);
            }

            // Compute the distances of each segment.
            totalDist = 0.0f;

            for (int valueIndex = 1; valueIndex < values.size(); valueIndex++) {
                totalDist += distance(values.get(valueIndex - 1).data, values.get(valueIndex).data);
                values.get(valueIndex).weight = totalDist;
            }

            // Compute the alpha value that represents when to use this segment.
            for (int iLoop = 1; iLoop < values.size(); iLoop++) {
                values.get(iLoop).weight /= totalDist;
            }
        }


        float distance(Vector3f lhs, Vector3f rhs) {
            return new Vector3f(rhs).sub(lhs).length();
        }
    }


    public static class WeightedLinearInterpolatorVec4 {
        public ArrayList<Data> values = new ArrayList<>();

        public class Data {
            public Vector4f data;
            public float weight;
        }


        public Vector4f interpolate(float alpha) {
            if (values.isEmpty()) return new Vector4f();

            if (values.size() == 1) return new Vector4f(values.get(0).data);

            // Find which segment we are within.
            int segment = 1;
            for (; segment < values.size(); segment++) {
                if (alpha < values.get(segment).weight) break;
            }
            if (segment == values.size()) return new Vector4f(values.get(segment - 1).data);

            float sectionAlpha = alpha - values.get(segment - 1).weight;
            sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;

            float invSecAlpha = 1.0f - sectionAlpha;
            return new Vector4f(values.get(segment - 1).data).mul(invSecAlpha).add(new Vector4f(values.get(segment).data).mul(sectionAlpha));
        }
    }
}
