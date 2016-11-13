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

import static javafx.scene.input.KeyCode.M;

/**
 * Implements a simple application that opens a 3D rendering window and
 * shows a rotating cube.
 */
public class Animation
{
    static RenderPanel renderPanel;
    static RenderContext renderContext;
    static Shader normalShader;
    static Shader diffuseShader;
    static Material material;
    static SimpleSceneManager sceneManager;
    static Shape cylindershape;
    static Shape torusshape;
    static float currentstep, basicstep;
    static int resolution=22;
    static float[] cylinderbottomcenter = {0,1,0};
    static double cylinderheight = 2;
    static float torusouterradius =1;
    static float torusinnerradius =2;
    static int torusoutres =240;
    static int torusinres =20;
    static Shape planeshape;
    static Matrix4f torusInitialTransformation;
    static Matrix4f cylinderInitialTransformation;
    static float animangle;

    /**
     * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to
     * provide a call-back function for initialization. Here we construct
     * a simple 3D scene and start a timer task to generate an animation.
     */
    public final static class SimpleRenderPanel extends GLRenderPanel
    {

        /**This method will initialize somewhat like a circling airplane with a spinning rotor
         * above a plane
         *
         * @param r
         */
        public void init(RenderContext r){
            sceneManager = new SimpleSceneManager();

            initTorus(r);
            torusInitialTransformation = torusshape.getTransformation();

            initCylinder(r);
            cylinderInitialTransformation = cylindershape.getTransformation();

            initPlane(r);

            scaleShape((float)0.25,torusshape);
            scaleShape((float)0.5,cylindershape);

            translatePlaneY(-2);
            rotatePlaneBack();
            rotateCylinderZ((float)Math.PI/2);
            rotateTorusY((float)Math.PI/2);

            translateCylinderAndTorus(0,(float) 0,-3);

            Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
        }

        private void translateCylinderX(float x) {
            Matrix4f t = cylindershape.getTransformation();
            Matrix4f transX  =new Matrix4f();
            Vector3f vx = new Vector3f(x,0,0);
            transX.setTranslation(vx);
            t.add(transX);
            cylindershape.setTransformation(t);
        }

        private void translateTorusX(float x) {
            Matrix4f t = torusshape.getTransformation();
            Matrix4f transX = new Matrix4f();

            Vector3f vx = new Vector3f(x,0,0);
            transX.setTranslation(vx);
            t.add(transX);
            torusshape.setTransformation(t);
        }


        private void translateCylinderAndTorus(float x,float y, float z){
            Matrix4f t = torusshape.getTransformation();
            Matrix4f c = cylindershape.getTransformation();

            Vector3f vx = new Vector3f(x,0,0);
            Vector3f vy = new Vector3f(0,y,0);
            Vector3f vz = new Vector3f(0,0,z);

            Matrix4f mx = new Matrix4f();
            mx.setTranslation(vx);

            Matrix4f my = new Matrix4f();
            my.setTranslation(vy);

            Matrix4f mz = new Matrix4f();
            mz.setTranslation(vz);

            t.add(mx);
            c.add(mx);

            t.add(my);
            c.add(my);

            t.add(mz);
            c.add(mz);

            torusshape.setTransformation(t);
            cylindershape.setTransformation(c);

        }

        private void rotatePlaneBack(){
            Matrix4f t = planeshape.getTransformation();
            Matrix4f rotX = new Matrix4f();
            rotX.rotX(-(float)Math.toRadians(80));
            t.mul(rotX);
            planeshape.setTransformation(t);
        }

        private void translateCylinderY(float y){
            Matrix4f t = cylindershape.getTransformation();
            Vector3f v  = new Vector3f(0,y,0);
            t.setTranslation(v);
            cylindershape.setTransformation(t);
        }

        private void translatePlaneY(float y){
            Matrix4f t = planeshape.getTransformation();
            Vector3f v  = new Vector3f(0,y,0);
            t.setTranslation(v);
            planeshape.setTransformation(t);
        }


        private void rotateTorusX(float angle){
            Matrix4f t = torusshape.getTransformation();
            Matrix4f rotX=new Matrix4f();
            rotX.rotX(angle);
            t.mul(rotX);
            torusshape.setTransformation(t);
        }

        private void scaleShape(float factor, Shape shape){
            Matrix4f t = shape.getTransformation();
            t.setScale(factor);
            shape.setTransformation(t);
        }

        private void rotateTorusY(float angle){
            Matrix4f t = torusshape.getTransformation();
            Matrix4f rotY=new Matrix4f();
            rotY.rotY(angle);
            t.mul(rotY);
            torusshape.setTransformation(t);
        }

        private void rotateCylinderZ(float angle){
            Matrix4f t = cylindershape.getTransformation();
            Matrix4f rotZ = new Matrix4f();
            rotZ.rotZ(angle);
            t.mul(rotZ);
            cylindershape.setTransformation(t);
        }

        /**
         * Initialization call-back. We initialize our renderer here.
         *
         * @param r	the render context that is associated with this render panel
         */
        public void initCylinder(RenderContext r)
        {
            renderContext = r;

            // The vertex positions of the cylinder
            float v[] = concatenateCylinderVertices();

            // The vertex normals
            float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
                    -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
                    0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
                    1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
                    0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
                    0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

            // The vertex colors
            float c[] = getCylinderVerticeColors(v);

            // Texture coordinates
            float uv[] = {0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1};

            // Construct a data structure that stores the vertices, their
            // attributes, and the triangle mesh connectivity
            VertexData vertexData = renderContext.makeVertexData(2+2*resolution);
            vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
            vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
            vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

            // The triangles (three vertex indices for each triangle)
            int indices[] = getCylinderIndices();	// bottom face

            vertexData.addIndices(indices);

            // Make a scene manager and add the object

            cylindershape = new Shape(vertexData);
            sceneManager.addShape(cylindershape);

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
         /*   Timer timer = new Timer();
            basicstep = 0.01f;
            currentstep = basicstep;
            timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);*/
        }

        public void initPlane(RenderContext r)
        {
            renderContext = r;

            // The vertex positions of the plane
            float v[] = {-3,-3,0,  3,-3,0, 3,3,0,  -3,3,0};

            // The vertex normals
            float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
                    -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
                    0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
                    1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
                    0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
                    0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

            // The vertex colors
            float c[] = {1,1,1,  1,1,1, 1,1,1,  1,1,1};

            // Texture coordinates
            float uv[] = {0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1,
                    0,0, 1,0, 1,1, 0,1};

            // Construct a data structure that stores the vertices, their
            // attributes, and the triangle mesh connectivity
            VertexData vertexData = renderContext.makeVertexData(4);
            vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
            vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
            vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
            vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);

            // The triangles (three vertex indices for each triangle)
            int indices[] = {
                    0,1,2,
                    0,2,3
            };

            vertexData.addIndices(indices);

            // Make a scene manager and add the object

            planeshape = new Shape(vertexData);
            sceneManager.addShape(planeshape);

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

        }

        public void initTorus(RenderContext r)
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
            int indices[] = calculateTorusIndices( torusinres, torusoutres);	// bottom face

            vertexData.addIndices(indices);

            // Make a scene manager and add the object
            sceneManager = new SimpleSceneManager();
            torusshape = new Shape(vertexData);
            sceneManager.addShape(torusshape);

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

        }

        /**Concatenates all the vertice arrays to one array.
         * @return
         */
        private float[] concatenateCylinderVertices(){
            float[] bottomvertices = getCylinderBottomVertices(cylinderbottomcenter);
            float[] topvertices = getCylinderTopVertices();
            float[] topcenter  = cylinderbottomcenter.clone();
            topcenter[1]+= cylinderheight;
            float[] vertices =concat(topcenter, cylinderbottomcenter);
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

        /** Calculates the vertices of a torus
         *
         * @param innerresolution the resolution of the inner, horizontal ring
         * @param outerresolution the resolution of the outer, vertical ring
         * @return the torus vertices
         */

        private float[] constructTorusVertices(int innerresolution, int outerresolution) {
            float[] vertices = new float[3 * innerresolution * outerresolution+3];
            double innerangle = 2 * Math.PI / innerresolution;
            double outerangle = 2 * Math.PI / outerresolution;
            vertices[0]=0;
            vertices[1]=0;
            vertices[2]=0;
            for (int i = 0; i < innerresolution; i++) {

                for (int j = 0; j < 3 * outerresolution; ) {
                    vertices[i * 3 * outerresolution + j+3] = torusinnerradius * (float) Math.cos(innerangle * i) + torusouterradius * (float) (Math.cos(innerangle * i) * Math.cos(outerangle * j / 3)); //x
                    vertices[i * 3 * outerresolution + j + 1+3] = torusinnerradius * (float) Math.sin(innerangle * i) + torusouterradius * (float) (Math.sin(innerangle * i) * Math.cos(outerangle * j / 3));//y
                    vertices[i * 3 * outerresolution + j + 2+3] = torusouterradius * (float) (Math.sin(outerangle * (j / 3))); //z
                    j += 3;
                }
            }
            return vertices;
        }

        /**Calculates the indices of the cylinder. It starts with the top triangles, then
         * the bottom ones and finally creates the side planes with triangles.
         *
         * @return
         */
        private int[] getCylinderIndices(){
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

        /** Transforms an ArrayList of ints into an int array
         *
         * @param indices
         * @return
         */
        private int[] transform(ArrayList<Integer> indices) {
            int[] array = new int[indices.size()];
            for(int i=0;i<indices.size();i++){
                array[i]=indices.get(i);
            }
            return array;
        }


        /**Calculates the vertices that are on the top side by adding
         * the height to the y-component of the bottom vertices
         *
         * @return all vertices of the top plane
         */

        private float[] getCylinderTopVertices(){
            float[] topvertices = getCylinderBottomVertices(cylinderbottomcenter);
            for(int i = 1;i<3*resolution;){

                topvertices[i]+= cylinderheight;
                i+=3;
            }
            return  topvertices;
        }
        /** Calculates the vertices at the bottom of the cylinder.
         *
         * @param bottomcenter The center point of the cylinder in the x/z axis.
         * @return an int array with the coordinates of the vertices, where the coordinates are saved
         * in the order of x,y,z
         */
        private float[] getCylinderBottomVertices(float[] bottomcenter){
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

        /**Calculates the indices for the triangles of the torus surface.
         * It starts at the most distant point to the center and then
         * creates the triangles that make the planes. Finally it closes
         * off with the triangles between the last and first outer rings
         *
         * @param innerres the resolution of the inner, horizontal ring
         * @param outerres the resolution of the outer, vertical rings
         * @return the indices as an int array
         */

        private int[] calculateTorusIndices(int innerres, int outerres){
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

        /**Gets the color for the vertices. Colors the bottom and top center black.
         * The rest is colored alternating in black and white. That way with an even number
         * as resolution it will get vertical lines and with an uneven number diagonal lines.
         *
         * @param vertices
         * @return
         */
        private float[] getCylinderVerticeColors(float[] vertices){
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
        private float[] getTorusVerticeColors(float[] vertices){
            float[] colors = new float[vertices.length];

            for(int i=0;i<torusinres;i++){
                if(i%2==0){
                    for(int j=0;j<3*torusoutres;j++){
                        colors[i*3*torusoutres+j]=1;
                    }
                }
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
            //Cylinder Animation
            animangle  = animangle+currentstep;

            Matrix4f c  = new Matrix4f(cylinderInitialTransformation);
            Matrix4f translatec0 = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 3, 0, 0, 0, 1);

            Matrix4f rotcX = new Matrix4f();
            rotcX.rotX(-animangle);

            c.mul(translatec0);
            c.mul(rotcX);
            translatec0.invert();
            c.mul(translatec0);

            cylindershape.setTransformation(c);
            
            //Torus Animation

            Matrix4f rottY = new Matrix4f();
            rottY.rotY(-animangle);

            Matrix4f t = new Matrix4f(torusInitialTransformation);
            Matrix4f translate0 = new Matrix4f(1, 0, 0, -3, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);

            Matrix4f rottZ=new Matrix4f();
            rottZ.rotZ(-animangle);

            t.mul(translate0);
            t.mul(rottY);
            translate0.invert();
            t.mul(translate0);
            t.mul(rottZ);

            torusshape.setTransformation(t);

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
                    cylindershape.setMaterial(null);
                    renderContext.useShader(normalShader);
                    break;
                }
                case 'd': {
                    // Remove material from cylindershape, and set "default" shader
                    cylindershape.setMaterial(null);
                    renderContext.useDefaultShader();
                    break;
                }
                case 'm': {
                    // Set a material for more complex shading of the cylindershape
                    if(cylindershape.getMaterial() == null) {
                        cylindershape.setMaterial(material);
                    } else
                    {
                        cylindershape.setMaterial(null);
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

