package simple;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;
import jrtr.swrenderer.SWRenderPanel;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and
 * shows a rotating cube.
 */
public class Landscape
{	static float[][] landscape = new float[(int) Math.pow(2,3)+1][(int) Math.pow(2,3)+1];

    static RenderPanel renderPanel;
    static RenderContext renderContext;
    static Shader normalShader;
    static Shader diffuseShader;
    static Material material;
    static SimpleSceneManager sceneManager;
    static Shape shape;
    static float currentstep, basicstep;
    static int width;
    static float[] vertices;
    static int[] indices;
    static float[] colors;
    static float factor = 4;
    static float delta = 1.7f;
    static float division = 2f;
    static Shape lastShape;
    static float[] normals;
    static float width1=500;
    static float height=500;

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
            createLandscape();
            renderContext = r;

            // Construct a data structure that stores the vertices, their
            // attributes, and the triangle mesh connectivity
            VertexData vertexData = renderContext.makeVertexData(landscape.length*landscape.length);
            vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
            vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
            vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);

            // The triangles (three vertex indices for each triangle)
            vertexData.addIndices(indices);

            // Make a scene manager and add the object
            sceneManager = new SimpleSceneManager();
            shape = new Shape(vertexData);
            sceneManager.addShape(shape);
            //translateShape(shape);

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

            translateShape(shape);

            // Register a timer task
            Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
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
                axis.normalize();
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
    static float x1;
    static float x2;
    static float y1;
    static float y2;
    static boolean calculated=true;


    public static float[] convertOnSphere(float[] point){
        float[] newPoint = new float[3];
        newPoint[0]=point[0]/(width1/2)-1;
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
     * A mouse listener for the main window of this application. This can be
     * used to process mouse events.
     */
    public static class SimpleMouseListener implements MouseMotionListener
    {


        public void mousePressed(MouseEvent e) {
            x1  =e.getX();
            y1 = e.getY();
            calculated = false;
        }
        public void mouseReleased(MouseEvent e) {


        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}

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
     * A key listener for the main window. Use this to process key events.
     * Currently this provides the following controls:
     * 's': stop animation
     * 'p': play animation
     * '+': accelerate rotation
     * '-': slow down rotation
     * 'd': default shader
     * 'n': shader using surface normals
     * 'm': use a material for shading
     */
    public static class SimpleKeyListener implements KeyListener
    {
        public void keyPressed(KeyEvent e)
        {
            switch(e.getKeyChar())
            {
                case 's': {
                    // Stop animation
                    currentstep = 0;
                    break;
                }
                case 'p': {
                    // Resume animation
                    currentstep = basicstep;
                    break;
                }
                case '+': {
                    // Accelerate roation
                    currentstep += basicstep;
                    break;
                }
                case '-': {
                    // Slow down rotation
                    currentstep -= basicstep;
                    break;
                }
                case 'n': {
                    // Remove material from cylindershape, and set "normal" shader
                    shape.setMaterial(null);
                    renderContext.useShader(normalShader);
                    break;
                }
                case 'd': {
                    // Remove material from cylindershape, and set "default" shader
                    shape.setMaterial(null);
                    renderContext.useDefaultShader();
                    break;
                }
                case 'm': {
                    // Set a material for more complex shading of the cylindershape
                    if(shape.getMaterial() == null) {
                        shape.setMaterial(material);
                    } else
                    {
                        shape.setMaterial(null);
                        renderContext.useDefaultShader();
                    }
                    break;
                }
            }

            // Trigger redrawing
            renderPanel.getCanvas().repaint();
        }

        public void keyReleased(KeyEvent e)
        {
        }

        public void keyTyped(KeyEvent e)
        {
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
        width=9;
        jframe.setSize(500, 500);
        jframe.setLocationRelativeTo(null); // toruscenter of screen
        jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

        // Add a mouse and key listener
        renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseListener());
        renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
        renderPanel.getCanvas().setFocusable(true);

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true); // show window
    }

    /** This turns the empty field landscape into a heightmap
     *
     *
     */
    public static void calculateLandscape(int length){

        int distance = (length-1)/2;

        while(distance>0){
            //square step
            for(int j=0;j<(length-1)/distance/2;j++){ //y
                for(int i=0;i<(length-1)/distance/2;i++){ //x
                    int y  = distance + j*2*distance;
                    int x = distance + i*2*distance;
                    getSquareHeight(landscape,y,x,distance);
                }
            }

            //diamond step
            for(int j=0;j<(length-1)/distance+1;j++){
                if(j%2==0){ // the diamond vertical of the squares
                    for(int i=0; i<(length-1)/distance/2;i++) {
                        int y = j * distance;
                        int x = distance + i * 2 * distance;
                        getDiamondHeight(landscape, y, x, distance);
                    }
                }
                else{
                    for(int i=0;i<(length-1)/distance/2+1;i++){
                        int y= j*distance;
                        int x = 2*i*distance;
                        getDiamondHeight(landscape,y,x,distance);
                    }
                }
            }
            distance/=2;
            delta /= division;
        }

    }

    private static void getDiamondHeight(float[][] landscape, int y, int x, int distance) {
        float heightleft= x>0? landscape[y][x-distance]:0;
        float heightright = x<landscape.length-1 ? landscape[y][x+distance]:0;
        float heighttop = y>0 ? landscape[y-distance][x]:0;
        float heightbottom = y <landscape[0].length-1 ? landscape[y+distance][x]:0;

        float avg = (heightbottom+heightleft+heightright+heighttop)/4;

        if(heightleft==0) avg = (heightbottom+heightright+heighttop)/3;
        if(heightbottom==0) avg = (heightleft+heightright+heighttop)/3;
        if(heightright==0) avg = (heightbottom+heightleft+heighttop)/3;
        if(heighttop==0) avg = (heightbottom+heightleft+heightright)/3;

        int randomn = (int)(Math.random()*100);

        avg+=Math.pow(-1,randomn)*(float)((2*Math.random()-1)*delta);
        landscape[y][x]=Math.abs(avg);
    }

    private static void getSquareHeight(float[][] landscape, int y, int x, int distance) {
        float a = landscape[y-distance][x-distance];
        float b = landscape[y-distance][x+distance];
        float c = landscape[y+distance][x-distance];
        float d = landscape[y+distance][x+distance];
        int randomn = (int)(Math.random()*100);
        landscape[y][x]= (float) Math.abs( (a+b+c+d)/ 4+ Math.pow(-1,randomn)*(2*Math.random()-1)*delta);
    }

    public static void initLandscape(int length){

        landscape = new float[length][length];
        landscape[0][0]=(float)Math.random()*factor+3;
        landscape[0][length-1]=(float)Math.random()*factor+3;
        landscape[length-1][0]=(float)Math.random()*factor+3;
        landscape[length-1][length-1]=(float)Math.random()*factor+3;
    }

    public static void createLandscape(){
        initLandscape(1025);
        calculateLandscape(1025);
        createVertices();
        createIndices();
        makeColor();
        calculateNormals();
    }

    private static void createVertices() {
        vertices=new float[3*landscape.length*landscape.length];
        ArrayList<Float> vers=new ArrayList<>();
        for(float i=0;i<landscape.length;i++){
            for(float j=0;j<landscape.length;j++){
                vers.add(j/100f);
                vers.add(landscape[(int)i][(int)j]);
                vers.add(i/100f);

            }
        }
        vertices=transformFloat(vers);
    }

    private static float[] transformFloat(ArrayList<Float> indices) {
        float[] array = new float[indices.size()];
        for(int i=0;i<indices.size();i++){
            array[i]=indices.get(i);
        }
        return array;
    }

    private static void createIndices(){
        ArrayList<Integer> inds=new ArrayList<>();
        for(int j=0;j<landscape.length-1;j++){
            for(int i = 0;i<landscape.length-1;i++) {
                int index1= i+j*landscape.length;
                int index2= i+1+ j*landscape.length;
                int index3= i+ (j+1)*landscape.length;
                int index4= i+1+(j+1)*landscape.length;

                inds.add(index1);
                inds.add(index2);
                inds.add(index3);
               /* inds.add(0);
                inds.add(0);
                inds.add(0);*/


                inds.add(index2);
                inds.add(index4);
                inds.add(index3);

            }
        }

        indices=transformInteger(inds);
    }

    public static int[] transformInteger(ArrayList<Integer> indices) {
        int[] array = new int[indices.size()];
        for(int i=0;i<indices.size();i++){
            array[i]=indices.get(i);
        }
        return array;
    }

    public static void makeColor(){
        colors = new float[vertices.length];
        int x=0;
        int y=0;
        for(int i=0;i<colors.length;i+=3) {
            if(landscape[y][x]>4.8f){
                colors[i]=255;
                colors[i+1]=255;
                colors[i+2]=255;
        }
            else{
                colors[i]=0;
                colors[i+1]=102;
                colors[i+2]=0;
            }
            x++;
            if(x==landscape.length){
                y++;
                x=0;
            }
        }
       /* for(int i=0;i<colors.length;i+=3){
            if(i%2==0) {
                colors[i] = 0;
                colors[i + 1] = 255;
                colors[i + 2] = 0;
            }
            else{
                colors[i]=0;
                colors[i+1]=255;
                colors[i+2]=255;
            }
        }*/

    }

    private static void translateShape(Shape shape){
        Matrix4f t = shape.getTransformation();
        Matrix4f m = new Matrix4f();
        Vector3f v= new Vector3f(-3,-6,-8);
        //m.setTranslation(v);
        //t.mul(m);
        t.setTranslation(v);
        shape.setTransformation(t);
    }

    private static void calculateNormals(){
        normals = new float[vertices.length];
        for (int i=0;i<vertices.length;i+=3){
            normals[i]=0;
            normals[i+1]=0;
            normals[i+2]=1;

        }

        Vector3f v = computeNormal(vertices[0],vertices[1],vertices[2],
                vertices[3], vertices[4], vertices[5], vertices[landscape.length], vertices[landscape.length+1], vertices[landscape.length+2]);
        normals[0]=v.x;
        normals[1]=v.y;
        normals[2]=v.z;

        for(int j=3;j<landscape.length-3;j+=3){
            Vector3f v1= computeNormal(vertices[j-3], vertices[j-2], vertices[j-1],
                    vertices[j], vertices[j+1],vertices[j+2],
                    vertices[j-3+landscape.length],vertices[j-2+landscape.length],vertices[j-1+landscape.length]);

            Vector3f v2= computeNormal(
                    vertices[j], vertices[j+1],vertices[j+2],
                    vertices[j-3+landscape.length],vertices[j-2+landscape.length],vertices[j-1+landscape.length],
                    vertices[j+landscape.length],vertices[j+1+landscape.length], vertices[j+2+landscape.length])
                    ;

            Vector3f v3= computeNormal(vertices[j+3], vertices[j+2], vertices[j+1],
                    vertices[j], vertices[j+1],vertices[j+2],
                    vertices[j+landscape.length],vertices[j+1+landscape.length],vertices[j+2+landscape.length]);

            v1.add(v2);
            v1.add(v3);

            v1.scale(1/3f);

            normals[j]=v1.x;
            normals[j+1]=v1.y;
            normals[j+2]=v1.z;

        }

        for(int j=landscape.length;j<vertices.length-landscape.length-3;j+=3){
            Vector3f v1 = computeNormal(vertices[j-3],vertices[j-2],vertices[j-1],
                    vertices[j],vertices[j+1],vertices[j+2],
                    vertices[j-landscape.length],vertices[j-landscape.length+1],vertices[j-landscape.length+2]);

            Vector3f v2 = computeNormal(
                    vertices[j],vertices[j+1],vertices[j+2],
                    vertices[j-landscape.length],vertices[j-landscape.length+1],vertices[j-landscape.length+2],
                    vertices[j-landscape.length+3],vertices[j-landscape.length+4],vertices[j-landscape.length+5]);

            Vector3f v3 = computeNormal(
                    vertices[j],vertices[j+1],vertices[j+2],
                    vertices[j+3],vertices[j+4],vertices[j+5],
                    vertices[j-landscape.length+3],vertices[j-landscape.length+4],vertices[j-landscape.length+5]);

            Vector3f v4 = computeNormal(                    vertices[j],vertices[j+1],vertices[j+2],
                    vertices[j+3],vertices[j+4],vertices[j+5],
                    vertices[j+landscape.length],vertices[j+landscape.length+1],vertices[j+landscape.length+2]);

            Vector3f v5 = computeNormal(
                    vertices[j],vertices[j+1],vertices[j+2],
                    vertices[j+landscape.length],vertices[j+landscape.length+1],vertices[j+landscape.length+2],
                    vertices[j+landscape.length-3],vertices[j+landscape.length-2],vertices[j+landscape.length-1]);

            Vector3f v6 = computeNormal(
                    vertices[j],vertices[j+1],vertices[j+2],
                    vertices[j-3],vertices[j-2],vertices[j-1],
                    vertices[j+landscape.length-3],vertices[j+landscape.length-2],vertices[j+landscape.length-1]);


            v1.add(v2);
            v1.add(v3);
            v1.add(v4);
            v1.add(v5);
            v1.add(v6);

            v1.scale((1/6f));

            normals[j]=v1.x;
            normals[j+1]=v1.y;
            normals[j+2]=v1.z;

        }
    }

    private static Vector3f computeNormal(float vertice, float vertice1, float vertice2, float vertice3, float vertice4, float vertice5, float vertice6, float vertice7, float vertice8) {
        Vector3f v1 = new Vector3f(vertice-vertice3,vertice1-vertice4,vertice2-vertice5);
        Vector3f v2 = new Vector3f(vertice-vertice6,vertice1-vertice7,vertice2-vertice8);

        Vector3f normal = new Vector3f();
        normal.cross(v1,v2);
        return normal;
    }
}
