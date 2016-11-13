package jrtr.swrenderer;

import jrtr.RenderContext;
import jrtr.RenderItem;
import jrtr.SceneManagerInterface;
import jrtr.SceneManagerIterator;
import jrtr.Shader;
import jrtr.Texture;
import jrtr.VertexData;
import jrtr.glrenderer.GLRenderPanel;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.*;
import java.nio.Buffer;
import java.util.LinkedList;
import java.util.ListIterator;


/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 
 * you will implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel} 
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer;
	private float[][] zBuffer;
		
	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;
	}
	
	/**
	 * This is called by the SWRenderPanel to render the scene to the 
	 * software frame buffer.
	 */
	public void display()
	{
		if(sceneManager == null) return;
		
		beginFrame();
	
		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext())
		{
			draw(iterator.next());
		}		
		
		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer()
	{
		return colorBuffer;
	}
	
	/**
	 * Set a new viewport size. The render context will also need to store
	 * a viewport matrix, which you need to reset here. 
	 */
	public void setViewportSize(int width, int height)
	{
		colorBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	}
		
	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame()
	{
		colorBuffer.createGraphics().clearRect(0,0, colorBuffer.getWidth(), colorBuffer.getHeight());
		zBuffer = new float[colorBuffer.getWidth()][colorBuffer.getHeight()];
	}
	
	private void endFrame()
	{		
	}
	
	/**
	 * The main rendering method. You will need to implement this to draw
	 * 3D objects.
	 */
	private void draw(RenderItem renderItem) {

		float[] c=null;
		int[] indices;
		float[] n;
		float[] tc;
		boolean hasTextures=false;
		float[] v=null;

		float width = colorBuffer.getWidth();
		float height = colorBuffer.getHeight();

		// Get reference to the vertex data of the render item to be rendered
		VertexData vertexData = renderItem.getShape()
				.getVertexData();

		LinkedList<VertexData.VertexElement> list = vertexData.getElements();


		indices= vertexData.getIndices();

		Matrix4f transformationMat = renderItem.getShape().getTransformation();
		Matrix4f camMat = sceneManager.getCamera().getCameraMatrix();
		Matrix4f frustum = sceneManager.getFrustum().getProjectionMatrix();
		Matrix4f viewportMat = new Matrix4f(
				(width-1)/2f,0,0, (width+1)/2f,
				0, -(height-1)/2f , 0, (height+1)/2f,
				0, 0, 1/2f, 1/2f,
				0, 0, 0, 1
		);

		int k=0;
		float[] positions = new float[9];
		float[][] verColors = new float[4][3];
		float[][] normals = new float[3][3];
		float[][] textureCoordinates = new float[3][2];

		if(true) {
			//Loop over all edges
			for (int j : indices) {

				ListIterator<VertexData.VertexElement> itr = list.listIterator(0);

				//Loop over all properties of an edge
				while (itr.hasNext()) {
					VertexData.VertexElement e = itr.next();

					if (e.getSemantic() == VertexData.Semantic.POSITION) {
						float[] vertices = e.getData();
						Vector4f point = new Vector4f(vertices[j * 3], vertices[j * 3 + 1], vertices[j * 3 + 2], 1f);
						transformationMat.transform(point);
						camMat.transform(point);
						frustum.transform(point);
						viewportMat.transform(point);

						positions[k * 3] = point.x;
						positions[k * 3 + 1] = point.y;
						positions[k * 3 + 2] = point.z;

						k++;
					}

					if (e.getSemantic() == VertexData.Semantic.COLOR) {
						c = e.getData();
						verColors[k][0] = c[j * 3];
						verColors[k][1] = c[j * 3 + 1];
						verColors[k][2] = c[j * 3 + 2];
					}

					if (e.getSemantic() == VertexData.Semantic.NORMAL) {
						n = e.getData();
						normals[k][0] = n[j * 3];
						normals[k][1] = n[j * 3 + 1];
						normals[k][2] = n[j * 3 + 2];
					}

					if (e.getSemantic() == VertexData.Semantic.TEXCOORD) {
						tc = e.getData();
						hasTextures = true;
						textureCoordinates[k][0] = tc[j * 2];
						textureCoordinates[k][1] = tc[j * 2 + 1];

					}

					if (k == 3) {
						Matrix3f pos = new Matrix3f(positions);

						//Bounding Box
						int[][] oldPos = {
								{(int) (pos.m00 / pos.m02), (int) (pos.m01 / pos.m02)},
								{(int) (pos.m10 / pos.m12), (int) (pos.m11 / pos.m12)},
								{(int) (pos.m20 / pos.m22), (int) (pos.m21 / pos.m22)}
						};

						int left = Math.min(Math.min(oldPos[0][0], oldPos[1][0]), oldPos[2][0]);
						int right = Math.max(Math.max(oldPos[0][0], oldPos[1][0]), oldPos[2][0]);
						int top = Math.min(Math.min(oldPos[0][1], oldPos[1][1]), oldPos[2][1]);
						int bottom = Math.max(Math.max(oldPos[0][1], oldPos[1][1]), oldPos[2][1]);

						left = left < 0 ? 0 : left + 1;
						right = right > width ? (int) width : right + 1;
						top = top < 0 ? 0 : top + 1;
						bottom = bottom > height ? (int) height : bottom + 1;

						pos.invert();

						//Going through Bounding Box
						//x
						for (int i = left; i < right; i++) {
							//y
							for (int l = top; l < bottom; l++) {

								float aw = pos.m00 * i + pos.m10 * l + pos.m20;
								float bw = pos.m01 * i + pos.m11 * l + pos.m21;
								float cw = pos.m02 * i + pos.m12 * l + pos.m22;

								if (aw > 0 && bw > 0 && cw > 0) {
									//Buffer
									Vector3f bh = new Vector3f(1, 1, 1);
									pos.transform(bh);
									float wFlip = bh.x * i + bh.y * l + bh.z;

									if (zBuffer[i][l] <= wFlip && !hasTextures) {

										//Color of the point
										float[] color = new float[3];
										//over the r,g and b value
										for (int col = 0; col < 3; col++) {
											Vector3f trans = new Vector3f(verColors[0][col], verColors[1][col], verColors[2][col]);
											pos.transform(trans);
											float uw = trans.x * i + trans.y * l + trans.z;
											color[col] = uw / wFlip;
										}

										colorBuffer.setRGB(i, l, new Color(color[0], color[1], color[2]).getRGB());
										zBuffer[i][l] = wFlip;
									} else if (zBuffer[i][l] <= wFlip) {

										SWTexture texture = (SWTexture) renderItem.getShape().getMaterial().texture;
										float[] realTextureCoordinate = new float[2];

										for (int tex = 0; tex < 2; tex++) {
											Vector3f texVec = new Vector3f(textureCoordinates[0][tex], textureCoordinates[1][tex], textureCoordinates[2][tex]);
											pos.transform(texVec);
											float uw = texVec.x * i + texVec.y * l + texVec.z;
											realTextureCoordinate[tex] = uw / wFlip;
										}
										int rgb = texture.getColorAt(realTextureCoordinate[0], 1 - realTextureCoordinate[1]);
										colorBuffer.setRGB(i, l, rgb);
										zBuffer[i][l] = wFlip;
									}
								}

							}
						}
						k = 0;
					}


				}
			}
		}


		else {
			ListIterator<VertexData.VertexElement> itr = list.listIterator(0);

			while (itr.hasNext()) {
				VertexData.VertexElement e = itr.next();

				if (e.getSemantic() == VertexData.Semantic.POSITION) {
					v = e.getData();
					for (int i = 0; i < v.length; i += 3) {
						transformPoint(v, i, i + 1, i + 2, renderItem.getShape().getTransformation());
						drawPoint(v, i, i + 1, i + 2);
					}
				}

				if (e.getSemantic() == VertexData.Semantic.COLOR) {
					c = e.getData();
				}

				if (e.getSemantic() == VertexData.Semantic.NORMAL) {
					n = e.getData();
				}
			}

			// Loop over all triangles
			for (int i = 0; i < indices.length; i += 3) {

				int ind1 = indices[i];
				int ind2 = indices[i + 1];
				int ind3 = indices[i + 2];

				Matrix3f edgeFunctions = computeEdgeFunctions(v, ind1, ind2, ind3);
				float[] boundingBoxVertices = getBoundingBoxVertices(v, ind1, ind2, ind3);
				float[] homDivCoefs = getHomDivCoef(v, ind1, ind2, ind3);
				transformPoint(v, ind1, ind2, ind3, renderItem.getShape().getTransformation());
				Matrix3f rgbFunctions = getRGBFunctions(v, c, ind1, ind2, ind3);
//				drawTriangle(edgeFunctions, boundingBoxVertices, rgbFunctions, homDivCoefs);

			}
		}



	}

	/** Returns the matrix with the coefficients. These are
	 * stored in the rows of the matrix in the order R,G,B
	 *
	 * @param v vertices
	 * @param c colors
	 * @param ind1 triangle vertex index
	 * @param ind2 triangle vertex index
	 * @param ind3 triangle vertex index
	 * @return the matrix with the coefficients for the rgb functions
	 */
	private Matrix3f getRGBFunctions(float[] v, float[] c, int ind1, int ind2, int ind3) {
		float x1 = v[3*ind1];
		float y1 = v[3*ind1+1];
		float z1 = v[3*ind1+2];

		float x2 = v[3*ind2];
		float y2 = v[3*ind2+1];
		float z2 = v[3*ind2+2];

		float x3 = v[3*ind3];
		float y3 = v[3*ind3+1];
		float z3 = v[3*ind3+2];

		float[] urCoefs = getCoefs(
				x1, y1, z1,
				x2, y2, z2,
				x3, y3, z3,
				c[3*ind1]*255, c[3*ind2]*255,c[3*ind3]*255
		);

		float[] ugCoefs = getCoefs(
				x1, y1, z1,
				x2, y2, z2,
				x3, y3, z3,
				c[3*ind1+1]*255, c[3*ind2+1]*255,c[3*ind3+1]*255
		);

		float[] ubCoefs = getCoefs(
				x1, y1, z1,
				x2, y2, z2,
				x3, y3, z3,
				c[3*ind1+2]*255, c[3*ind2+2]*255,c[3*ind3+2]*255

		);

		return new Matrix3f(
				urCoefs[0],urCoefs[1],urCoefs[2],
				ugCoefs[0],ugCoefs[1],ugCoefs[2],
				ubCoefs[0],ubCoefs[1],ubCoefs[2]
		);
	}

	/** Gets the coefficients for the function
	 * for the homogeneous division stored in the order
	 * a,b,c
	 *
	 * @param v the vertices
	 * @param ind1 triangle vertice index
	 * @param ind2 triangle vertice index
	 * @param ind3 triangle vertice index
	 * @return array with coefficients for hom. Div
	 */
	private float[] getHomDivCoef(float[] v, int ind1, int ind2, int ind3) {
		float x1 = v[3*ind1];
		float y1 = v[3*ind1+1];
		float z1 = v[3*ind1+2];

		float x2 = v[3*ind2];
		float y2 = v[3*ind2+1];
		float z2 = v[3*ind2+2];

		float x3 = v[3*ind3];
		float y3 = v[3*ind3+1];
		float z3 = v[3*ind3+2];

		Matrix3f verCor = new Matrix3f(
				x1,y1,z1,
				x2,y2,z2,
				x3,y3,z3
		);

		verCor.invert();
		Vector3f vector = new Vector3f(1,1,1);

		verCor.transform(vector);

		return new float[]{vector.x,vector.y,vector.z};
	}


	private float[] getCoefs(float x1, float y1, float w1, float x2, float y2, float w2, float x3, float y3, float w3, float u1, float u2, float u3) {
		Matrix3f verCor = new Matrix3f(
				x1,y1,w1,
				x2,y2,w2,
				x3,y3,w3
		);

		Vector3f u = new Vector3f(u1,u2,u3);

		verCor.invert();
		verCor.transform(u);

		return new float[]{u.x,u.y,u.z};
	}

	private void drawTriangle(Matrix3f edgeFunctions, float[] boundingBoxVertices, Matrix3f rgbFunctions, float[] homDivCoefs) {
		//over the rows in the bounding box:y
		for(float i=boundingBoxVertices[2];i<=boundingBoxVertices[0];i++){
			//over the columns in the bounding box:x
			for(float j=boundingBoxVertices[3];j<=boundingBoxVertices[1];j++){
				float aw = edgeFunctions.m00*j + edgeFunctions.m10*i + edgeFunctions.m20;
				float bw = edgeFunctions.m01*j + edgeFunctions.m11*i + edgeFunctions.m21;
				float cw = edgeFunctions.m02*j + edgeFunctions.m12*i + edgeFunctions.m22;

				if(aw>0 && bw >0 && cw>0){
					float zBuf = getHomDiv(homDivCoefs,j,i);
					int color = getColor(rgbFunctions,j,i, zBuf);
					drawPoint(j,i);
				}

			}
		}


	}

	private float getHomDiv(float[] homDivCoefs, float j, float i) {
		return homDivCoefs[0]*j+homDivCoefs[1]*i+homDivCoefs[2];
	}

	private int getColor(Matrix3f rgbFunctions, float j, float i, float zBuf) {
		float r = (rgbFunctions.m00*j+rgbFunctions.m01*i+rgbFunctions.m02)/zBuf;
		float g = (rgbFunctions.m10*j+rgbFunctions.m11*i+rgbFunctions.m12)/zBuf;
		float b = (rgbFunctions.m20*j+rgbFunctions.m21*i+rgbFunctions.m22)/zBuf;
		Color color = new Color((r),g,b);

		return color.getRGB();

	}


	/** Computes the edge functions and stores them in the matrix.
	 * The functions are stored in the columns
	 *
	 * @param v
	 * @param ind1
	 * @param ind2
	 * @param ind3
	 * @return
	 */
	private Matrix3f computeEdgeFunctions(float[] v, int ind1, int ind2, int ind3) {
		float v1x = v[3*ind1];
		float v1y = v[3*ind1+1];
		float v1z = 1;// v[3*ind1+2];

		float v2x = v[3*ind2];
		float v2y = v[3*ind2+1];
		float v2z = 1;//v[3*ind2+2];

		float v3x = v[3*ind3];
		float v3y = v[3*ind3+1];
		float v3z = 1;//v[3*ind3+2];

		Matrix3f coeffMat = new Matrix3f(
				v1x, v1y, v1z,
				v2x, v2y, v2z,
				v3x,v3y,v3z
		);

		coeffMat.invert();

		return coeffMat;

	}

	/** The edge values of the bounding box are computed and
	 * stored in an array in the order top, right, bottom, left
	 *
	 * @param v the vertices
	 * @param ind1 edge 1 of the triangle
	 * @param ind2 edge 2 of the triangle
	 * @param ind3 edge 3 of the triangle
	 * @return the edge values of the bounding box
	 */
	private float[] getBoundingBoxVertices(float[] v, int ind1, int ind2, int ind3) {
		float v1x = v[3*ind1];
		float v1y = v[3*ind1+1];

		float v2x = v[3*ind2];
		float v2y = v[3*ind2+1];

		float v3x = v[3*ind3];
		float v3y = v[3*ind3+1];

		float maxX = colorBuffer.getWidth();//getMax(v1x,v2x,v3x);
		float minX = 0;//getMin(v1y,v2y,v3y);
		float maxY = colorBuffer.getHeight();//getMax(v1y,v2y,v3y);
		float minY = 0;//getMin(v1x,v2x,v3x);

		return new float[] {maxY,maxX,minY,minX};
	}

	private float getMin(float v1, float v2, float v3) {
		if(v1<= v2 && v1 <= v3){
			return v1;
		}
		else if(v2<= v1 && v2 <= v3){
			return v2;
		}
		else{
			return v3;
		}
	}

	private float getMax(float v1x, float v2x, float v3x) {
		if(v1x>= v2x && v1x >= v3x){
			return v1x;
		}
		else if(v2x>= v1x && v2x >= v3x){
			return v2x;
		}
		else{
			return v3x;
		}
	}


	/** Draws the point into the BufferedImage
	 *
	 * @param v the vertices
	 * @param i position of x coordinate
	 * @param i1 position of y coordinate
	 * @param i2 position of z coordinate
	 */
	private void drawPoint(float[] v, int i, int i1, int i2) {

		if (0<=v[i] && v[i]<=colorBuffer.getWidth() && 0<=v[i+1] && v[i+1] <= colorBuffer.getHeight()) {
			colorBuffer.setRGB((int) (v[i]) * 1, (int) (v[i1]) * 1, 16777215);

		}
	}

	private void drawPoint(float x, float y) {
		if (0<x && x<colorBuffer.getWidth() && 0<y && y < colorBuffer.getHeight()) {
			colorBuffer.setRGB((int) x, (int) y, 16777215);
		}
	}

	/** Transforms a vertice from object coordinate to pixel coordinates
	 *  @param v the vertices
	 * @param i position of x coordinate
	 * @param i1 position of y coordinate
	 * @param i2 position of z coordinate
	 * @param transformation
	 */
	private void transformPoint(float[] v, int i, int i1, int i2, Matrix4f transformation) {
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
		vector.x *=colorBuffer.getWidth();
		vector.y = (vector.y + 1)/2f;
		vector.y *=colorBuffer.getHeight();
		vector.y = colorBuffer.getHeight()-1-vector.y;

		v[i]=vector.x;
		v[i+1]=vector.y;
		v[i+2]=vector.z;

	}

	/** Multiplies a vector with a Matrix
	 *
	 * @param vector
	 * @param mat
	 */
	private Vector4f mulMatVec(Vector4f vector, Matrix4f mat) {
		float x = mat.m00*vector.x + mat.m01*vector.y + mat.m02*vector.z;
		float y = mat.m10*vector.x + mat.m11*vector.y + mat.m12*vector.z;
		float z = mat.m20*vector.x + mat.m21*vector.y + mat.m22*vector.z;

		return  new Vector4f(x,y,z,vector.w);
	}

	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader()	
	{
		return new SWShader();
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useShader(Shader s)
	{
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useDefaultShader()
	{
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture()
	{
		return new SWTexture();
	}
	
	public VertexData makeVertexData(int n)
	{
		return new SWVertexData(n);		
	}


}
