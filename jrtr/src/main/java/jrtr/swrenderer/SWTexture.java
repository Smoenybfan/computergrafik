package jrtr.swrenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import jrtr.Texture;

import javax.imageio.ImageIO;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {

	private BufferedImage texture;

	public void load(String fileName) throws IOException {

		File f = new File(fileName);
		texture = ImageIO.read(f);
	}

	public BufferedImage getTexture(){
		return this.texture;
	}

	public int getColorAt(float u, float v){
		u*=texture.getWidth()-1;
		v*=texture.getHeight()-1;
		float[] nearestNeighbour = getNearestNeighbour(u,v);

		float[] usedPoint = nearestNeighbour;
		//set to true for bilinear, false for neighbour
		if(true) {
			return getBilinearInterpolated(u,v);
		}
		return texture.getRGB((int)usedPoint[0],(int) usedPoint[1]);
	}

	//Inefficient as fuck, I could just round but... meh
	private float[] getNearestNeighbour(float u, float v) {
		float[] nearest = new float[2];
		nearest[0]=Math.round(u);
		nearest[1]=Math.round(v);

		return  nearest;
	}

	private int getBilinearInterpolated(float u, float v) {


		int u0 = (int)Math.floor(u);
		int v0 = (int)Math.floor(v);
		int u1 = u0+1;
		int v1 = v0+1;
		if (u1 > texture.getWidth()-1)
			u1 = texture.getWidth()-1;
		if (v1 > texture.getHeight()-1)
			v1 = texture.getHeight()-1;
		float wu = u - u0;
		float wv = v - v0;
		int[] col = new int[3];
		int[] neighbours = {u0, u1, v0, v1};
		int[][] nearPoints = new int[4][3];
		for (int i = 0; i < 4; i++) {
			nearPoints[i][0] = new Color(texture.getRGB(neighbours[i%2], neighbours[2+i/2])).getRed();
			nearPoints[i][1] = new Color(texture.getRGB(neighbours[i%2], neighbours[2+i/2])).getGreen();
			nearPoints[i][2] = new Color(texture.getRGB(neighbours[i%2], neighbours[2+i/2])).getBlue();
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j< 3; j++) {
				if (nearPoints[i][j] < 0)
					nearPoints[i][j] = 0;
				else if (nearPoints[i][j] > 255)
					nearPoints[i][j] = 255;
			}
		}

		for (int i = 0; i < 3; i++) {
			float cb = nearPoints[0][i] * (1 - wu) + nearPoints[1][i] * wu;
			float ct = nearPoints[2][i] * (1 - wu) + nearPoints[3][i] * wu;
			col[i] = Math.round(cb*(1-wv)+ct*wv);
		}
		Color color = new Color(col[0], col[1], col[2]);
		int rgb = color.getRGB();

		return rgb;
	}

}
