package simple;

import jrtr.*;
import jrtr.glrenderer.*;
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
public class LearnScene
{
    static RenderPanel renderPanel;
    static RenderContext renderContext;
    static Shader normalShader;
    static Shader diffuseShader;
    static Material material;
    static SimpleSceneManager sceneManager;
    static Shape shape;
    static float currentstep, basicstep;
    static int resolution=30;
    static Shape floor;
    static Material floorMaterial;

    /**
     * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to
     * provide a call-back function for initialization. Here we construct
     * a simple 3D scene and start a timer task to generate an animation.
     */
    public final static class SimpleRenderPanel extends GLRenderPanel
    {
        float[] bottomcenter = {0,1,0};
        int height = 2;
        /**
         * Initialization call-back. We initialize our renderer here.
         *
         * @param r	the render context that is associated with this render panel
         */
        public void init(RenderContext r)
        {
            renderContext = r;
            bottomcenter = new float[]{0,1,0};
            height = 2;

            floor = makeFloor();


            // The vertex positions of the cylinder
            float v[] = concatenateVertices();

            // The vertex normals
            float n[] = getTextureCoordinates((int) (2f/3f*v.length));		// bottom face

            // The vertex colors
            float c[] = getVerticeColors(v);

            // Texture coordinates
            float uv[] = getTextureCoordinates(v.length);

            // Construct a data structure that stores the vertices, their
            // attributes, and the triangle mesh connectivity
            VertexData vertexData = renderContext.makeVertexData(2+2*resolution);
            vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
            vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
            vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

            // The triangles (three vertex indices for each triangle)
            int indices[] = getIndices();	// bottom face

            vertexData.addIndices(indices);

            // Make a scene manager and add the object
            sceneManager = new SimpleSceneManager();
            shape = new Shape(vertexData);
            sceneManager.addShape(shape);

            sceneManager.addShape(floor);

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

/*
            Light light = new Light();
            light.type= Light.Type.SPOT;
            light.diffuse=new Vector3f(1,0,0);
            light.direction=new Vector3f(1,-1,0);
            light.position=new Vector3f(-5,0,-15);
            sceneManager.addLight(light);

            Light light1 = new Light();
            light1.type= Light.Type.SPOT;
            light1.diffuse=new Vector3f(1f,1f,1f);
            light1.diffuse.scale(0.5f);
            light1.direction = new Vector3f(0,0,0);
            Vector3f position = new Vector3f(5,3,-15);
            //sceneManager.getCamera().getCameraMatrix().transform(position);
            light1.position = position;
            sceneManager.addLight(light1);
*/
            Light light2 = new Light();
            light2.type= Light.Type.SPOT;
            light2.diffuse=new Vector3f(1f,1f,1f);
            light2.diffuse.scale(50f);
            light2.direction = new Vector3f(0,0,0);
            Vector3f position = new Vector3f(0,3,-20);
            //sceneManager.getCamera().getCameraMatrix().transform(position);
            light2.position = position;
            sceneManager.addLight(light2);

            diffuseShader = renderContext.makeShader();
            try {
                diffuseShader.load("../jrtr/shaders/myShader.vert", "../jrtr/shaders/myShader.frag");
            } catch(Exception e) {
                System.out.print("Problem with shader:\n");
                System.out.print(e.getMessage());
            }

            floorMaterial=new Material();
            floorMaterial.diffuseMap=renderContext.makeTexture();
            floorMaterial.shader=diffuseShader;
            floorMaterial.texture = renderContext.makeTexture();
            try {
                floorMaterial.texture.load("../textures/stone.jpg");
                floorMaterial.diffuseMap.load("../textures/stone.jpg");
            } catch (Exception e){
                System.out.print("Scho no schad chani di textur ni lade he");
                System.out.print(e.getMessage());
            }
            floor.setMaterial(floorMaterial);

            // Make a material that can be used for shading
            material = new Material();
            material.shader = diffuseShader;
            material.diffuseMap=renderContext.makeTexture();
            material.diffuseMap = renderContext.makeTexture();
            try {
                material.diffuseMap.load("../textures/stone.jpg");
            } catch(Exception e) {
                System.out.print("Could not load texture.\n");
                System.out.print(e.getMessage());
            }

            // Register a timer task
            Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);


        }

        private float[] getTextureCoordinates(int length) {
            float[] texCoor = new float[(int) (2.f/3.f *length)];
            texCoor[0]=0.5f;
            texCoor[1]=0.5f;
            texCoor[2]=0.5f;
            texCoor[3]=0.5f;

            for(int i=4;i<2/3*length;i+=2){
                if(i<=length/3){
                    texCoor[i] = (float)i/(float)resolution;
                    texCoor[i+1]=1;
                }
                else{
                    texCoor[i] = (float)i/(float)resolution;
                    texCoor[i+1]=0;
                }
                i++;

            }

            return texCoor;
        }

        /**Concatenates all the vertice arrays to one array.
         * @return
         */
        private float[] concatenateVertices(){
            float[] bottomvertices = getBottomVertices(bottomcenter);
            float[] topvertices = getTopVertices();
            float[] topcenter  =bottomcenter.clone();
            topcenter[1]+=height;
            float[] vertices =concat(topcenter,bottomcenter);
            vertices=concat(vertices,topvertices);
            vertices=concat(vertices,bottomvertices);

            return vertices;
        }

        /**Concats two arrays
         *
         * @param a
         * @param b
         * @return
         */
        public float[] concat(float[] a, float[] b) {
            int aLen = a.length;
            int bLen = b.length;
            float[] c= new float[aLen+bLen];
            System.arraycopy(a, 0, c, 0, aLen);
            System.arraycopy(b, 0, c, aLen, bLen);
            return c;
        }

        private int[] getIndices(){
            ArrayList<Integer> indices = new ArrayList<>();

            for(int i=2;i<=resolution;i++){
                //top triangles
                indices.add(0);
                indices.add(i);
                indices.add(i+1);
                if(i==resolution){
                    indices.add(0);

                    indices.add(i+1);
                    indices.add(2);
                }
            }

            for(int i=2;i<=resolution;i++) {
                //bottom triangles
                indices.add(1);
                indices.add(i + resolution);
                indices.add(i + 1 + resolution);
                if (i == resolution) {
                    indices.add(1);
                    indices.add(i + 1 + resolution);
                    indices.add(2 + resolution);
                }
            }

            for(int i=2;i<=resolution;i++){
                //side first triangle
                indices.add(i);
                indices.add(i+1);
                indices.add(i+resolution);
                if(i==resolution){
                    indices.add(i+1);
                    indices.add(i+resolution+1);
                    indices.add(2);

                }
                //side second triangle
                indices.add(i+resolution);
                indices.add(i+1+resolution);
                indices.add(i+1);

                if(i==resolution){
                    indices.add((i+resolution+1));
                    indices.add(2+resolution);
                    indices.add(2);
                }
            }

            int[] inds = transform(indices);

            return inds;
        }

        private int[] transform(ArrayList<Integer> indices) {
            int[] array = new int[indices.size()];
            for(int i=0;i<indices.size();i++){
                array[i]=indices.get(i);
            }
            return array;
        }


        /**Calculates the vertices that are on the top side
         *
         * @return all vertices of the top plane
         */

        private float[] getTopVertices(){
            float[] topvertices = getBottomVertices(bottomcenter);
            for(int i = 1;i<3*resolution;){

                topvertices[i]+=height;
                i+=3;
            }
            return  topvertices;
        }

        public static Shape makeFloor(){
            float[] vertices = {-20,-4,20, 20,-4,20, 20,-4,-20, -20,-4,-20};

            float[] colors  ={ 0,0.5f,0, 0,0.5f,0, 0,0.5f,0, 0,0.5f,0,			// ground floor
            };
            float[] normals = { 0,1,0,  0,1,0,  0,1,0,  0,1,0,		// ground floor
            };

            float[] t = {0,0,  1,0,  1,1,  0,1};
            int[] indices = {0,1,3, 1,2,3};

            // Set up the vertex data
            VertexData vertexData = renderContext.makeVertexData(4);

            // Specify the elements of the vertex data:
            // - one element for vertex positions
            vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
            // - one element for vertex colors
            vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
            // - one element for vertex normals
            vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(t, VertexData.Semantic.TEXCOORD,2);



            floor=new Shape(vertexData);

            vertexData.addIndices(indices);

            Shape floor = new Shape(vertexData);

            return floor;

        }

        /** Calculates the vertices at the bottom of the cylinder.
         *
         * @param bottomcenter The toruscenter point of the cylinder in the x/z axis.
         * @return an int array with the coordinates of the vertices, where the coordinates are saved
         * in the order of x,y,z
         */
        private float[] getBottomVertices(float[] bottomcenter){
            double angle = 2*Math.PI/resolution;
            double length = 1;//Math.sqrt(Math.pow(cylinderbottomcenter[0],2)+Math.pow(cylinderbottomcenter[1],2)+Math.pow(cylinderbottomcenter[2],2));

            float[] vertices = new float[3*resolution];
            vertices[0]=bottomcenter[0]+1;
            vertices[1]=bottomcenter[1];
            vertices[2]=bottomcenter[2];


            for(int i=3;i<3*resolution;){
                vertices[i]= bottomcenter[0]+ (float) (Math.cos(angle*i/3)/length);
                vertices[i+1]= bottomcenter[1];
                vertices[i+2]= bottomcenter[2]+ (float) (Math.sin(-angle*i/3)/length);
                i+=3;
            }

            return  vertices;
        }

        private float[] getVerticeColors(float[] vertices){
            float[] colors = new float[vertices.length];
            for(int i=0;i<6;i++){
                colors[i]=1;
            }

            for(int i=6;i<vertices.length;){
                if(i%2==0){
                    colors[i]=0;
                    colors[i+1]=0;
                    colors[i+2]=0;
                }
                else{
                    colors[i]=1;
                    colors[i+1]=1;
                    colors[i+2]=1;

                }

                i+=3;
            }
            return colors;
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
            /*/ Update transformation by rotating with angle "currentstep"
            Matrix4f t = shape.getTransformation();
            Matrix4f rotX = new Matrix4f();
            rotX.rotX(currentstep);
            Matrix4f rotY = new Matrix4f();
            rotY.rotY(currentstep);
            Matrix4f rotz = new Matrix4f();
            rotz.rotZ(currentstep);
            t.mul(rotX);
            t.mul(rotY);
            t.mul(rotz);
            shape.setTransformation(t);*/

            // Trigger redrawing of the render window
            //renderPanel.getCanvas().repaint();
        }
    }

    /**
     * A mouse listener for the main window of this application. This can be
     * used to process mouse events.
     */
    public static class SimpleMouseListener implements MouseListener
    {
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}
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
            switch(Character.toLowerCase(e.getKeyChar()))
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
                    if(floor.getMaterial() == null) {
                        shape.setMaterial(material);
                        floor.setMaterial(floorMaterial);
                       // renderContext.useShader(floorMaterial.shader);
                    } else
                    {
                        shape.setMaterial(null);
                        floor.setMaterial(null);
                        renderContext.useDefaultShader();
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
        jframe.setSize(500, 500);
        jframe.setLocationRelativeTo(null); // toruscenter of screen
        jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

        // Add a mouse and key listener
        renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
        renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
        renderPanel.getCanvas().setFocusable(true);

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true); // show window
    }


}

