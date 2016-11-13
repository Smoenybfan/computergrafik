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
public class Torus
{
    static RenderPanel renderPanel;
    static RenderContext renderContext;
    static Shader normalShader;
    static Shader diffuseShader;
    static Material material;
    static SimpleSceneManager sceneManager;
    static Shape shape;
    static float currentstep, basicstep;

    /**
     * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to
     * provide a call-back function for initialization. Here we construct
     * a simple 3D scene and start a timer task to generate an animation.
     */
    public final static class SimpleRenderPanel extends SWRenderPanel
    {
        float torusouterradius =1;
        float torusinnerradius =2;
        int torusoutres =300;
        int torusinres =400;
        float[] toruscenter ={0,0,0};
        /**
         * Initialization call-back. We initialize our renderer here.
         *
         * @param r	the render context that is associated with this render panel
         */
        public void init(RenderContext r)
        {
            renderContext = r;


            // The vertex positions of the torus
            float v[] = constructTorusVertices(torusinres, torusoutres);	// bottom face

            // The vertex normals
            float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
                    -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
                    0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
                    1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
                    0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
                    0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

            // The vertex colors
            float c[] = getTorusVerticeColors(v);

            // Texture coordinates
            float uv[] = {0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1};

            // Construct a data structure that stores the vertices, their
            // attributes, and the triangle mesh connectivity
            VertexData vertexData = renderContext.makeVertexData(torusinres * torusoutres+1);
            vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
            vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
            vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

            // The triangles (three vertex indices for each triangle)
            int indices[] = calculateTorusIndices(v, torusinres, torusoutres);	// bottom face

            vertexData.addIndices(indices);

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

            // Register a timer task
            Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
        }

        private float[] constructTorusVertices(int innerresolution, int outerresolution){
            float[] vertices = new float[3*innerresolution*outerresolution+3];
            double innerangle=2*Math.PI/innerresolution;
            double outerangle=2*Math.PI/outerresolution;
           for(int i=0;i<innerresolution;i++){

                for(int j=0;j<3*outerresolution;){
                    vertices[i*3*outerresolution+j+3]= torusinnerradius *(float) Math.cos(innerangle*i)+ torusouterradius *(float)(Math.cos(innerangle*i)*Math.cos(outerangle*j/3)); //x
                    vertices[i*3*outerresolution+j+1+3]= torusinnerradius *(float)Math.sin(innerangle*i)+ torusouterradius *(float)(Math.sin(innerangle*i)*Math.cos(outerangle*j/3));//y
                    vertices[i*3*outerresolution+j+2+3]= torusouterradius *(float) (Math.sin(outerangle*(j/3))); //z
                    j+=3;

                }
           }

            return vertices;
        }

        private int[] calculateTorusIndices(float[] vertices, int innerres, int outerres){
            ArrayList<Integer> inds = new ArrayList<>();

            for(int i = 0;i<innerres-1;i++){

                for(int j=0;j<outerres-1;j++){

                    inds.add(i*outerres+j+1);
                    inds.add(i*outerres+j+1+1);
                    inds.add(i*outerres+j+outerres+1);

                    inds.add(i*outerres+j+1+1);
                    inds.add(i*outerres+j+outerres+1);
                    inds.add(i*outerres+j+1+outerres+1);

                    if(j==outerres-2){
                        inds.add(i*outerres+outerres-1+1);
                        inds.add(i*outerres+1);
                        inds.add(i*outerres+outerres+j+1+1);
                    }

                    if(j==outerres-2){
                        inds.add(i*outerres+1);
                        inds.add(i*outerres+outerres+j+1+1);
                        inds.add(i*outerres+outerres+1);
                    }
                }

                if(i==innerres-2){
                    for(int k=0;k<outerres-1;k++){
                        inds.add((i+1)*outerres+k+1);
                        inds.add((i+1)*outerres+k+1+1);
                        inds.add(k+1);

                        inds.add((i+1)*outerres+k+1+1);
                        inds.add(k+1);
                        inds.add(k+1+1);
                    }

                    inds.add((i+1)*outerres+outerres-1+1);
                    inds.add((i)*outerres+1);
                    inds.add(0+1);

                    inds.add((i+1)*outerres+1);
                    inds.add(0+1);
                    inds.add(1+1);


                }


            }
            int[] indices = transform(inds);
            return indices;
        }

        private int[] transform(ArrayList<Integer> indices) {
            int[] array = new int[indices.size()];
            for(int i=0;i<indices.size();i++){
                array[i]=indices.get(i);
            }
            return array;
        }

        private float[] getTorusVerticeColors(float[] vertices){
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
            // Update transformation by rotating with angle "currentstep"
            Matrix4f t = shape.getTransformation();
            Matrix4f rotX = new Matrix4f();
            rotX.rotX(currentstep);
            Matrix4f rotY = new Matrix4f();
            rotY.rotY(currentstep);
            t.mul(rotX);
            t.mul(rotY);
            shape.setTransformation(t);

            // Trigger redrawing of the render window
            renderPanel.getCanvas().repaint();
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
