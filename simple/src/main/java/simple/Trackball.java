package simple;

import jrtr.*;
import jrtr.glrenderer.GLRenderPanel;
import jrtr.swrenderer.SWRenderPanel;

import javax.swing.*;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.event.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by smoen on 12.10.2016.
 */
public class Trackball {

    private static float ballWidth=2;
    private static float ballHeight=2;
    private static float ballRadius=1;

    static RenderPanel renderPanel;
    static RenderContext renderContext;
    static Shader normalShader;
    static Shader diffuseShader;
    static Material material;
    static SimpleSceneManager sceneManager;
    static Shape shape;
    static float currentstep, basicstep;
    static float x1;
    static float x2;
    static float y1;
    static float y2;
    static boolean calculated;
    static Shape lastShape;
    static int width=500;
    static int height =500;


    public static float[] convertOnSphere(float[] point){
        float[] newPoint = new float[3];
        newPoint[0]=point[0]/(width/2)-1;
        newPoint[1]=1-point[1]/(height/2);
       // newPoint[0]=(float)(newPoint[0]/Math.sqrt(Math.pow(newPoint[0],2)+Math.pow(newPoint[1],2)));
     //   newPoint[1]=(float)(newPoint[1]/Math.sqrt(Math.pow(newPoint[0],2)+Math.pow(newPoint[1],2)));
        float z2 =(float)(1-Math.pow(newPoint[0],2)-Math.pow(newPoint[1],2));
        newPoint[2]= z2>=0 ? (float) Math.sqrt(z2) : 0;
        if(Float.isNaN(z2)){
            newPoint[2]=0;
        }
        return newPoint;
    }

    public static float getAngle(float[] point1, float[] point2){
        Vector3f v1 = new Vector3f(point1[0],point1[1],point1[2]);
        v1.scale(1/v1.length());
        Vector3f v2 = new Vector3f(point2[0],point2[1],point2[2]);
        v2.scale(1/v2.length());
        float angle = (float) Math.acos((v1.dot(v2)));
        if(Float.isNaN(angle)){
            angle=0;
        }
        return angle;
    }

    public static Vector3f getRotationAxis(float[] point1, float[] point2){
        Vector3f v1 = new Vector3f(point1[0],point1[1],point1[2]);
        v1.normalize();
        if(Float.isNaN(v1.x) || Float.isNaN(v1.y)|| Float.isNaN(v1.z)) {
            v1.x = 0;
            v1.y = 0;
            v1.z = 0;
        }
        Vector3f v2 = new Vector3f(point2[0],point2[1],point2[2]);
        v2.normalize();
        if(Float.isNaN(v2.x) || Float.isNaN(v2.y)|| Float.isNaN(v2.z)) {
            v2.x = 0;
            v2.y = 0;
            v2.z = 0;
        }
        v2.cross(v1,v2);
        return v2;
    }


    public static Matrix4f getRotationMatrixAroundAxis(Vector3f a, float angle){
        float cA = (float)Math.cos((angle));
        float sA = (float)Math.sin((angle));
        Matrix4f rt = new Matrix4f(
                (float) Math.pow(a.x,2)+ cA*(float)(1-Math.pow(a.x,2)) , a.x*a.y*(1-cA)-a.z*sA , a.x*a.z*(1-cA)+a.y*sA , 0 ,
                (a.x*a.y*(1-cA)+a.z*sA), (float)(Math.pow(a.y,2)+ cA*(1-Math.pow(a.y,2))) ,  (a.y*a.z*(1-cA)-a.x*sA) , 0 ,
                a.x*a.z*(1-cA)-a.y*sA , a.y*a.z*(1-cA)+a.x*sA , (float) (Math.pow(a.z,2)+ cA*(1-Math.pow(a.z,2))), 0  ,
                0 , 0 , 0 , 1
        );
        return rt;
    }

    /**
     * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to
     * provide a call-back function for initialization. Here we construct
     * a simple 3D scene and start a timer task to generate an animation.
     */
    public final static class SimpleRenderPanel extends SWRenderPanel
    {
        /**
         * Initialization call-back. We initialize our renderer here.
         *
         * @param r	the render context that is associated with this render panel
         */
        public void init(RenderContext r)
        {
            VertexData vertexData;
            renderContext = r;
            vertexData=renderContext.makeVertexData(1);
            ObjReader reader = new ObjReader();

            try {
                vertexData = ObjReader.read("../obj/teapot.obj",2,renderContext);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Make a simple geometric object: a cube

            // The vertex positions of the cube
            // Make a scene manager and add the object
            sceneManager = new SimpleSceneManager();
            shape = new Shape(vertexData);
            sceneManager.addShape(shape);

            // Add the scene to the renderer
            renderContext.setSceneManager(sceneManager);

            // Load some more shaders
            normalShader = renderContext.makeShader();
            try {
                normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
            } catch(Exception e) {
                System.out.print("Problem with shader:\n");
                System.out.print(e.getMessage());
            }

            diffuseShader = renderContext.makeShader();
            try {
                diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
            } catch(Exception e) {
                System.out.print("Problem with shader:\n");
                System.out.print(e.getMessage());
            }

            renderContext.useShader(normalShader);

            // Make a material that can be used for shading
            material = new Material();
            material.shader = diffuseShader;
            material.diffuseMap = renderContext.makeTexture();
            try {
                material.diffuseMap.load("../textures/plant.jpg");
            } catch(Exception e) {
                System.out.print("Could not load texture.\n");
                System.out.print(e.getMessage());
            }

            //Add some lights

          /*  Light light = new Light();
            light.diffuse=new Vector3f(0,0,150);
            light.position=new Vector3f(2,1,0);
            sceneManager.addLight(light);*/

            // Register a timer task
            Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
            calculated=true;
        }
    }

    /**
     * A timer task that generates an animation. This task triggers
     * the redrawing of the 3D scene every time it is executed.
     */
    public static class AnimationTask extends TimerTask
    {
        public void run()
        {
            if(calculated==false){
                float[] p1 = {x1,y1,0};
                float[] ps1 = convertOnSphere(p1);
                float[] p2 = {x2,y2,0};
                float[] ps2 = convertOnSphere(p2);

                float angle = getAngle(ps1,ps2);
                Vector3f axis = getRotationAxis(ps1,ps2);
                if(axis.length()!=0) {
                    axis.normalize();
                }
                if(Float.isNaN(axis.x) || Float.isNaN(axis.y)|| Float.isNaN(axis.z)){
                    axis.x=0;
                    axis.y=0;
                    axis.z=0;
                }
                if(lastShape==null){
                    lastShape=shape;
                }

                Matrix4f rot = getRotationMatrixAroundAxis(axis,angle);
                Matrix4f lt = lastShape.getTransformation();
                rot.mul(lt);
                shape.setTransformation(rot);

                renderPanel.getCanvas().repaint();
                x1=x2;
                y1=y2;
                calculated=true;

            }
        }
    }

    public static class SimpleMouseListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastShape=shape;
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    /**
     * A mouse listener for the main window of this application. This can be
     * used to process mouse events.
     */
    public static class SimpleMouseMotionListener implements MouseMotionListener
    {



        @Override
        public void mouseDragged(MouseEvent e) {

            x2= e.getX();
            y2 = e.getY();
            calculated=false;
        }

        @Override
        public void mouseMoved(MouseEvent e) {

            x1=e.getX();
            y1=e.getY();

        }
    }



    /**
     * The main function opens a 3D rendering window, implemented by the class
     * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed
     * for initialization automatically. It then constructs a simple 3D scene,
     * and starts a timer task to generate an animation.
     */
    public static void main(String[] args)
    {
        // Make a render panel. The init function of the renderPanel
        // (see above) will be called back for initialization.
        renderPanel = new SimpleRenderPanel();


        // Make the main window of this application and add the renderer to it
        JFrame jframe = new JFrame("simple");
        jframe.setSize(width, height);
        jframe.setLocationRelativeTo(null); // toruscenter of screen
        jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

        // Add a mouse and key listener
        renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener());
       // renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
        renderPanel.getCanvas().setFocusable(true);

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true); // show window
    }

    /** Takes the point from the 2D plane and calculates the point
     * on the sphere
     *
     * @param point
     * @return
     */

}
