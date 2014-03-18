package fcagnin.gltut.tut16;

import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Interpolators.ConstVelLinearInterpolatorVec3;
import fcagnin.gltut.framework.Interpolators.WeightedLinearInterpolatorFloat;
import fcagnin.gltut.framework.Interpolators.WeightedLinearInterpolatorVec4;
import fcagnin.gltut.framework.Timer;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * @author integeruser, xire-
 */
public class LightEnv {
    LightEnv(String envFileName) {
        // crea il parser e ci associa il file di input
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            String filepath = Framework.findFileOrThrow( envFileName );

            doc = dBuilder.parse( ClassLoader.class.getResourceAsStream( filepath ) );
        } catch ( SAXException | ParserConfigurationException | IOException e ) {
            e.printStackTrace();
            System.exit( -1 );
        }

        Element lightenvElement = doc.getDocumentElement();

        Element sunNode = (Element) lightenvElement.getElementsByTagName( "sun" ).item( 0 );
        if ( sunNode == null ) {
            throw new RuntimeException( "There must be a 'lightenv' element that has a 'sun' element as a child." );
        }

        float timerTime = Float.parseFloat( sunNode.getAttribute( "time" ) );

        sunTimer = new Timer( Timer.Type.LOOP, timerTime );

        ArrayList<LightData> ambient = new ArrayList<>();
        ArrayList<LightData> light = new ArrayList<>();
        ArrayList<LightData> background = new ArrayList<>();
        ArrayList<MaxIntensityData> maxIntensity = new ArrayList<>();

        {
            NodeList keys = sunNode.getElementsByTagName( "key" );
            Element key;
            int countKeys = 0;
            while ( (key = (Element) keys.item( countKeys )) != null ) {
                float keyTime = Float.parseFloat( key.getAttribute( "time" ) );
                //Convert from hours to normalized time.
                keyTime = keyTime / 24.0f;

                String strVec4 = key.getAttribute( "ambient" );
                ambient.add( new LightData( parseVec4( strVec4 ), keyTime ) );

                strVec4 = key.getAttribute( "intensity" );
                light.add( new LightData( parseVec4( strVec4 ), keyTime ) );

                strVec4 = key.getAttribute( "background" );
                background.add( new LightData( parseVec4( strVec4 ), keyTime ) );

                maxIntensity.add( new MaxIntensityData( Float.parseFloat( key.getAttribute( "max-intensity" ) ), keyTime ) );

                countKeys++;
            }
        }

        ambientInterpolator.setValues( ambient );
        sunlightInterpolator.setValues( light );
        backgroundInterpolator.setValues( background );
        maxIntensityInterpolator.setValues( maxIntensity );

        {
            NodeList lights = lightenvElement.getElementsByTagName( "light" );
            Element elemlight;
            int countLights = 0;
            while ( (elemlight = (Element) lights.item( countLights )) != null ) {

                if ( lightPos.size() + 1 == MAX_NUMBER_OF_LIGHTS ) { throw new RuntimeException( "Too many lights specified." ); }

                float lightTime = Float.parseFloat( elemlight.getAttribute( "time" ) );
                lightTimers.add( new Timer( Timer.Type.LOOP, lightTime ) );

                String strVec4 = elemlight.getAttribute( "intensity" );
                lightIntensity.add( parseVec4( strVec4 ) );

                ArrayList<Vec3> posValues = new ArrayList<>();
                NodeList keys = elemlight.getElementsByTagName( "key" );
                Element key;
                int countKeys = 0;
                while ( (key = (Element) keys.item( countKeys )) != null ) {
                    String text = key.getChildNodes().item( 0 ).getNodeValue();
                    posValues.add( parseVec3( text ) );
                    countKeys++;
                }

                if ( posValues.isEmpty() ) { throw new RuntimeException( "'light' elements must have at least one 'key' element child." ); }

                ConstVelLinearInterpolatorVec3 lightInterpolatorVec3 = new ConstVelLinearInterpolatorVec3();
                lightInterpolatorVec3.setValues( posValues );
                lightPos.add( lightInterpolatorVec3 );
                countLights++;
            }
        }
    }


    ////////////////////////////////
    private final static int MAX_NUMBER_OF_LIGHTS = 4;

    private final float halfLightDistance = 70.0f;
    private final float lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

    private Timer sunTimer;

    private TimedLinearInterpolatorVec4 ambientInterpolator = new TimedLinearInterpolatorVec4();
    private TimedLinearInterpolatorVec4 backgroundInterpolator = new TimedLinearInterpolatorVec4();
    private TimedLinearInterpolatorVec4 sunlightInterpolator = new TimedLinearInterpolatorVec4();
    private TimedLinearInterpolatorFloat maxIntensityInterpolator = new TimedLinearInterpolatorFloat();

    private ArrayList<ConstVelLinearInterpolatorVec3> lightPos = new ArrayList<>();
    private ArrayList<Vec4> lightIntensity = new ArrayList<>();
    private ArrayList<Timer> lightTimers = new ArrayList<>();


    ////////////////////////////////
    class PerLight extends BufferableData<FloatBuffer> {
        Vec4 cameraSpaceLightPos;
        Vec4 lightIntensity;

        static final int SIZE = Vec4.SIZE + Vec4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            cameraSpaceLightPos.fillBuffer( buffer );
            lightIntensity.fillBuffer( buffer );

            return buffer;
        }
    }

    class LightBlock extends BufferableData<FloatBuffer> {
        Vec4 ambientIntensity;
        float lightAttenuation;
        float maxIntensity;
        float padding[] = new float[2];
        PerLight lights[] = new PerLight[MAX_NUMBER_OF_LIGHTS];

        static final int SIZE = Vec4.SIZE + ((1 + 1 + 2) * (Float.SIZE / Byte.SIZE)) + PerLight.SIZE * MAX_NUMBER_OF_LIGHTS;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            ambientIntensity.fillBuffer( buffer );
            buffer.put( lightAttenuation );
            buffer.put( maxIntensity );
            buffer.put( padding );

            for ( PerLight light : lights ) {
                if ( light == null ) { break; }

                light.fillBuffer( buffer );
            }

            return buffer;
        }
    }


    ////////////////////////////////
    class TimedLinearInterpolatorFloat extends WeightedLinearInterpolatorFloat {

        public void setValues(ArrayList<MaxIntensityData> data) {
            setValues( data, true );
        }

        public void setValues(ArrayList<MaxIntensityData> data, boolean isLooping) {
            values.clear();

            for ( MaxIntensityData curr : data ) {
                Data temp = new Data();
                temp.data = getValue( curr );
                temp.weight = getTime( curr );

                values.add( temp );
            }

            if ( isLooping && !values.isEmpty() ) {
                Data temp = new Data();
                temp.data = values.get( 0 ).data;
                temp.weight = values.get( 0 ).weight;

                values.add( temp );
            }

            // Ensure first is weight 0, and last is weight 1.
            if ( !values.isEmpty() ) {
                values.get( 0 ).weight = 0.0f;
                values.get( values.size() - 1 ).weight = 1.0f;
            }
        }
    }

    class TimedLinearInterpolatorVec4 extends WeightedLinearInterpolatorVec4 {

        public void setValues(ArrayList<LightData> data) {
            setValues( data, true );
        }

        public void setValues(ArrayList<LightData> data, boolean isLooping) {
            values.clear();

            for ( LightData curr : data ) {
                Data temp = new Data();
                temp.data = new Vec4( getValue( curr ) );
                temp.weight = getTime( curr );

                values.add( temp );
            }

            if ( isLooping && !values.isEmpty() ) {
                Data temp = new Data();
                temp.data = new Vec4( values.get( 0 ).data );
                temp.weight = values.get( 0 ).weight;

                values.add( temp );
            }

            // Ensure first is weight 0, and last is weight 1.
            if ( !values.isEmpty() ) {
                values.get( 0 ).weight = 0.0f;
                values.get( values.size() - 1 ).weight = 1.0f;
            }
        }
    }


    ////////////////////////////////
    class Pair<K, V> {
        K first;
        V second;
    }


    class MaxIntensityData extends Pair<Float, Float> {
        public MaxIntensityData(Float first, Float second) {
            this.first = first;
            this.second = second;
        }
    }

    class LightData extends Pair<Vec4, Float> {
        public LightData(Vec4 first, Float second) {
            this.first = first;
            this.second = second;
        }
    }


    static Vec4 getValue(LightData data) {
        return data.first;
    }

    static float getTime(LightData data) {
        return data.second;
    }


    float getValue(MaxIntensityData data) {
        return data.first;
    }

    static float getTime(MaxIntensityData data) {
        return data.second;
    }


    ////////////////////////////////
    void updateTime(double elapsedTime) {
        sunTimer.update( (float) elapsedTime );

        for ( Timer timer : lightTimers ) {
            timer.update( (float) elapsedTime );
        }
    }


    void togglePause() {
        boolean isPaused = sunTimer.togglePause();
        setPause( isPaused );
    }

    void setPause(boolean pause) {
        sunTimer.setPause( pause );

        for ( Timer timer : lightTimers ) {
            timer.setPause( pause );
        }
    }


    void rewindTime(float secRewind) {
        sunTimer.rewind( secRewind );

        for ( Timer timer : lightTimers ) {
            timer.rewind( secRewind );
        }
    }

    void fastForwardTime(float secFF) {
        sunTimer.fastForward( secFF );

        for ( Timer timer : lightTimers ) {
            timer.fastForward( secFF );
        }
    }


    ////////////////////////////////
    LightBlock getLightBlock(Mat4 worldToCameraMat) {
        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = ambientInterpolator.interpolate( sunTimer.getAlpha() );
        lightData.lightAttenuation = lightAttenuation;
        lightData.maxIntensity = maxIntensityInterpolator.interpolate( sunTimer.getAlpha() );

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = Mat4.mul( worldToCameraMat, getSunlightDirection() );
        lightData.lights[0].lightIntensity = sunlightInterpolator.interpolate( sunTimer.getAlpha() );

        for ( int lightIndex = 0; lightIndex < lightPos.size(); lightIndex++ ) {
            Vec4 worldLightPos = new Vec4( lightPos.get( lightIndex ).interpolate( lightTimers.get( lightIndex ).getAlpha() ), 1.0f );
            Vec4 lightPosCameraSpace = Mat4.mul( worldToCameraMat, worldLightPos );

            lightData.lights[lightIndex + 1] = new PerLight();
            lightData.lights[lightIndex + 1].cameraSpaceLightPos = lightPosCameraSpace;
            lightData.lights[lightIndex + 1].lightIntensity = new Vec4( lightIntensity.get( lightIndex ) );
        }

        return lightData;
    }


    Vec4 getSunlightDirection() {
        float angle = 2.0f * 3.14159f * sunTimer.getAlpha();
        Vec4 sunDirection = new Vec4( 0.0f );
        sunDirection.x = (float) Math.sin( angle );
        sunDirection.y = (float) Math.cos( angle );

        // Keep the sun from being perfectly centered overhead.
        sunDirection = Mat4.mul( Glm.rotate( new Mat4( 1.0f ), 5.0f, new Vec3( 0.0f, 1.0f, 0.0f ) ), sunDirection );

        return sunDirection;
    }

    Vec4 getSunlightScaledIntensity() {
        return Vec4.scale( sunlightInterpolator.interpolate( sunTimer.getAlpha() ),
                1.0f / maxIntensityInterpolator.interpolate( sunTimer.getAlpha() ) );
    }


    int getNumLights() {
        return 1 + lightPos.size();
    }

    int getNumPointLights() {
        return lightPos.size();
    }


    Vec4 getPointLightIntensity(int pointLightIndex) {
        return lightIntensity.get( pointLightIndex );
    }

    Vec4 getPointLightScaledIntensity(int pointLightIndex) {
        return Vec4.scale( lightIntensity.get( pointLightIndex ),
                1.0f / maxIntensityInterpolator.interpolate( sunTimer.getAlpha() ) );
    }

    Vec3 getPointLightWorldPos(int pointLightIndex) {
        return lightPos.get( pointLightIndex ).interpolate( lightTimers.get( pointLightIndex ).getAlpha() );
    }


    Vec4 getBackgroundColor() {
        return backgroundInterpolator.interpolate( sunTimer.getAlpha() );
    }


    ////////////////////////////////
    private Vec4 parseVec4(String s) {
        Scanner snr = new Scanner( s );
        Vec4 res = new Vec4();

        res.x = Float.parseFloat( snr.next() );
        res.y = Float.parseFloat( snr.next() );
        res.z = Float.parseFloat( snr.next() );
        res.w = Float.parseFloat( snr.next() );

        snr.close();
        return res;
    }

    private Vec3 parseVec3(String s) {
        Scanner snr = new Scanner( s );
        Vec3 res = new Vec3();

        res.x = Float.parseFloat( snr.next() );
        res.y = Float.parseFloat( snr.next() );
        res.z = Float.parseFloat( snr.next() );

        snr.close();
        return res;
    }
}