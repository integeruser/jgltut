package jgltut.framework;

import jglsdk.glm.Glm;
import jglsdk.glm.Vec3;
import jglsdk.glm.Vec4;

import java.util.ArrayList;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
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
            Vec3 data;
            float weight;
        }


        public Vec3 interpolate(float alpha) {
            if (values.isEmpty()) return new Vec3();

            if (values.size() == 1) return new Vec3(values.get(0).data);

            // Find which segment we are within.
            int segment = 1;
            for (; segment < values.size(); segment++) {
                if (alpha < values.get(segment).weight) break;
            }
            if (segment == values.size()) return new Vec3(values.get(segment - 1).data);

            float sectionAlpha = alpha - values.get(segment - 1).weight;
            sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;

            float invSecAlpha = 1.0f - sectionAlpha;
            return Vec3.scale(values.get(segment - 1).data, invSecAlpha).add(Vec3.scale(values.get(segment).data, sectionAlpha));
        }
    }

    public static class ConstVelLinearInterpolatorVec3 extends WeightedLinearInterpolatorVec3 {
        float totalDist;

        public void setValues(ArrayList<Vec3> data) {
            setValues(data, true);
        }

        public void setValues(ArrayList<Vec3> data, boolean isLoop) {
            values.clear();

            for (Vec3 curr : data) {
                Data currData = new Data();
                currData.data = new Vec3(curr);
                currData.weight = 0.0f;
                values.add(currData);
            }

            if (isLoop) {
                Data currData = new Data();
                currData.data = new Vec3(data.get(0));
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


        float distance(Vec3 lhs, Vec3 rhs) {
            return Glm.length(Vec3.sub(rhs, lhs));
        }
    }


    public static class WeightedLinearInterpolatorVec4 {
        public ArrayList<Data> values = new ArrayList<>();

        public class Data {
            public Vec4 data;
            public float weight;
        }


        public Vec4 interpolate(float alpha) {
            if (values.isEmpty()) return new Vec4();

            if (values.size() == 1) return new Vec4(values.get(0).data);

            // Find which segment we are within.
            int segment = 1;
            for (; segment < values.size(); segment++) {
                if (alpha < values.get(segment).weight) break;
            }
            if (segment == values.size()) return new Vec4(values.get(segment - 1).data);

            float sectionAlpha = alpha - values.get(segment - 1).weight;
            sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;

            float invSecAlpha = 1.0f - sectionAlpha;
            return Vec4.scale(values.get(segment - 1).data, invSecAlpha).add(Vec4.scale(values.get(segment).data, sectionAlpha));
        }
    }
}