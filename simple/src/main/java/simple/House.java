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
public class House
{
    static RenderPanel renderPanel;
    static RenderContext renderContext;
    static Shader normalShader;
    static Shader diffuseShader;
    static Material material;
    static Material floorMaterial;
    static Material roofMaterial;
    static SimpleSceneManager sceneManager;
    static Shape shape;
    static Shape floor;
    static Shape roof;
    static float currentstep, basicstep;
    static int resolution=106;

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




            // Make a scene manager and add the object
            sceneManager = new SimpleSceneManager(2);
            shape = makeHouse();
            floor =makeFloor();
            roof=makeRoof();
            sceneManager.addShape(shape);
            sceneManager.addShape(floor);
            sceneManager.addShape(roof);

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
                diffuseShader.load("../jrtr/shaders/myShader.vert", "../jrtr/shaders/myShader.frag");
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
            material.texture = renderContext.makeTexture();
            try {
                material.texture.load("../textures/glurak.jpg");
            } catch (Exception e){
                System.out.print("Scho no schad chani di textur ni lade he");
                System.out.print(e.getMessage());
            }
            shape.setMaterial(material);

            floorMaterial=new Material();
            floorMaterial.shader=(diffuseShader);
            floorMaterial.texture = renderContext.makeTexture();
            try {
                floorMaterial.texture.load("../textures/stone.jpg");
            } catch (Exception e){
                System.out.print("Scho no schad chani di textur ni lade he");
                System.out.print(e.getMessage());
            }
            floor.setMaterial(floorMaterial);

            roofMaterial=new Material();
            roofMaterial.shader=diffuseShader;
            roofMaterial.texture = renderContext.makeTexture();
            try {
                roofMaterial.texture.load("../textures/roof.jpg");
            } catch (Exception e){
                System.out.print("Scho no schad chani di textur ni lade he");
                System.out.print(e.getMessage());
            }
            roof.setMaterial(roofMaterial);


            // Register a timer task
            Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);

            Light light = new Light();
           /* light.type= Light.Type.SPOT;
            light.diffuse=new Vector3f(200000,0,0);
            light.direction=new Vector3f(1,0,0);
            light.position=new Vector3f(-3,0,0);*/
            sceneManager.addLight(light);
        }


        private Shape makeRoof() {
             float vertices[] = {-4,4,4, 4,4,4, 0,8,4,				// the roof
                    4,4,4, 4,4,-4, 0,8,-4, 0,8,4,
                    -4,4,4, 0,8,4, 0,8,-4, -4,4,-4,
                    4,4,-4, -4,4,-4, 0,8,-4};

            float normals[] = { 0,0,1,  0,0,1,  0,0,1,				// front roof
                    0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, // right roof
                    -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, // left roof
                    0,0,-1, 0,0,-1, 0,0,-1};				// back roof

            float colors[] = {                    1,0,1, 0,0,1, 1,0,1,							// roof
                    1,0,0, 1,1,0, 1,0,0, 1,0,0,
                    0,1,1, 1,1,0, 1,0,1, 1,0,0,
                    0,0,1, 0,0,1, 0,0,1,};

            float[] t = {
                    0,0,  0,1, 1,1,  1,0,
                    0,0, 0,1, 1,1,  1,0,
                    0,0,  0,1,  1,1, 1,0,
                    1,1, 1,1

            };

            // Set up the vertex data
            VertexData vertexData = renderContext.makeVertexData(14);

            // Specify the elements of the vertex data:
            // - one element for vertex positions
            vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
            // - one element for vertex colors
            vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
            // - one element for vertex normals
            vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(t, VertexData.Semantic.TEXCOORD,2);

            roof=new Shape(vertexData);


            // The index data that stores the connectivity of the triangles
            int indices[] = {
                    28-28,29-28,30-28,				// roof
                    31-28,33-28,34-28, 31-28,32-28,33-28,
                    35-28,37-28,38-28, 35-28,36-28,37-28,
                    39-28,40-28,41-28};

            vertexData.addIndices(indices);

            Shape house = new Shape(vertexData);

            return house;
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

        public static Shape makeHouse()
        {
            // A house
            float vertices[] = {-4,-4,4, 4,-4,4, 4,4,4, -4,4,4,		// front face
                    -4,-4,-4, -4,-4,4, -4,4,4, -4,4,-4, // left face
                    4,-4,-4,-4,-4,-4, -4,4,-4, 4,4,-4,  // back face
                    4,-4,4, 4,-4,-4, 4,4,-4, 4,4,4,		// right face
                    4,4,4, 4,4,-4, -4,4,-4, -4,4,4,		// top face
                    -4,-4,4, -4,-4,-4, 4,-4,-4, 4,-4,4 // bottom face

                    //-20,-4,20, 20,-4,20, 20,-4,-20, -20,-4,-20, // ground floor
                /*    -4,4,4, 4,4,4, 0,8,4,				// the roof
                    4,4,4, 4,4,-4, 0,8,-4, 0,8,4,
                    -4,4,4, 0,8,4, 0,8,-4, -4,4,-4,
                    4,4,-4, -4,4,-4, 0,8,-4*/};

            float normals[] = {0,0,1,  0,0,1,  0,0,1,  0,0,1,		// front face
                    -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
                    0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
                    1,0,0,  1,0,0,  1,0,0,  1,0,0,		// right face
                    0,1,0,  0,1,0,  0,1,0,  0,1,0,		// top face
                    0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0		// bottom face

                    //0,1,0,  0,1,0,  0,1,0,  0,1,0,		// ground floor
                  /*  0,0,1,  0,0,1,  0,0,1,				// front roof
                    0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, // right roof
                    -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, // left roof
                    0,0,-1, 0,0,-1, 0,0,-1*/};				// back roof

            float colors[] = {0,0,1, 0,0,1, 1,0,0, 1,0,0,
                    0,1,1, 0,1,1, 0,1,0, 0,1,1,
                    1,0,0, 1,0,0, 1,0,0, 1,0,0,
                    0,0,1, 0,0,1, 0,0,1, 0,0,1,
                    0,0,1, 0,0,1, 0,0,1, 0,0,1,
                    0,0,1, 0,0,1, 0,0,1, 0,0,1

                   // 0,0.5f,0, 0,0.5f,0, 0,0.5f,0, 0,0.5f,0,			// ground floor
                   /* 0,0,1, 0,0,1, 0,0,1,							// roof
                    1,0,0, 1,0,0, 1,0,0, 1,0,0,
                    0,1,0, 0,1,0, 0,1,0, 0,1,0,
                    0,0,1, 0,0,1, 0,0,1,*/};

            float[] t = {
                    1,0,  0,0,  0,1, 1,1,
                    1,0,  0,0,  0,1, 1,1,
                    1,0,  0,0,  0,1, 1,1,
                    1,0,  0,0,  0,1, 1,1,
                    1,0,  0,0,  0,1, 1,1,
                    1,0,  0,0,  0,1, 1,1

                    //  0,0,  0,1,  1,1, 1,0,

                   /* 0,0,  0,1,  1,1,
                    0,0, 0,1, 1,1,  1,0,
                    0,0,  0,1,  1,1, 1,0,
                    0,0,  0,1,  1,1*/
            };

            // Set up the vertex data
            VertexData vertexData = renderContext.makeVertexData(24);

            // Specify the elements of the vertex data:
            // - one element for vertex positions
            vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
            // - one element for vertex colors
            vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
            // - one element for vertex normals
            vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(t, VertexData.Semantic.TEXCOORD,2);

            shape=new Shape(vertexData);


            // The index data that stores the connectivity of the triangles
            int indices[] = {0,2,3, 0,1,2,			// front face
                    4,6,7, 4,5,6,			// left face
                    8,10,11, 8,9,10,		// back face
                    12,14,15, 12,13,14,	// right face
                    16,18,19, 16,17,18,	// top face
                    20,22,23, 20,21,22,	// bottom face

                    //24,26,27, 24,25,26,	// ground floor
                  /*  28-4,29-4,30-4,				// roof
                    31-4,33-4,34-4, 31-4,32-4,33-4,
                    35-4,37-4,38-4, 35-4,36-4,37-4,
                    39-4,40-4,41-4*/};

            vertexData.addIndices(indices);

            Shape house = new Shape(vertexData);

            return house;
        }

        private static float[] getTextureCoordinates(float[] vertices) {
            float[] t = new float[2/3*vertices.length];
            int j=0;
            for (int i=0; i < vertices.length;i+=3){
                transformPoint(vertices,i,i+1,i+2,shape.getTransformation());
                vertices[i]/=500;
                vertices[i+1]/=500;
                vertices[i]+=0.5;
                vertices[i+1]+=0.5;
                vertices[i+1]=1-vertices[i+1];
                t[j]=vertices[i];
                t[j]=vertices[j+1];
                j+=2;
            }
        return t;

        }

        /** Transforms a vertice from object coordinate to pixel coordinates
         *  @param v the vertices
         * @param i position of x coordinate
         * @param i1 position of y coordinate
         * @param i2 position of z coordinate
         * @param transformation
         */
        private static void transformPoint(float[] v, int i, int i1, int i2, Matrix4f transformation) {
            Vector4f vector = new Vector4f(v[i], v[i1], v[i2],1);
            Matrix4f cam = sceneManager.getCamera().getCameraMatrix();
            Matrix4f frustum = sceneManager.getFrustum().getProjectionMatrix();

            //PCMp
            transformation.transform(vector);
            cam.transform(vector);
            frustum.transform(vector);

            //Homogeneous Division
            vector.x=vector.x/vector.w;
            vector.y=vector.y/vector.w;

            //D
            vector.x = (vector.x + 1)/2f;
            vector.x *=500;
            vector.y = (vector.y + 1)/2f;
            vector.y *=500;
            vector.y = 500-1-vector.y;

            v[i]=vector.x;
            v[i+1]=vector.y;
            v[i+2]=vector.z;

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
        jframe.setSize(1500, 1500);
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

