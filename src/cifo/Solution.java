package cifo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Random;

public class Solution {

	public static final int VALUES_PER_TRIANGLE = 10;

	protected ProblemInstance instance;
	protected int[] values;
	protected double fitness;
	protected double shared_fitness;
	protected Random r;
	protected double sharingCoefficient;

	private BufferedImage solutionImage;

	public Solution(ProblemInstance instance) {
		this.instance = instance;
		r = new Random();
		initialize();
	}

	public void initialize() {
		values = new int[instance.getNumberOfTriangles() * VALUES_PER_TRIANGLE];
		// initialize array of values

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			// initialize HSB and Alpha
			// generate for values for the colors
			for (int i = 0; i < 4; i++) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			}
			// initialize vertices
			// initialize triangle vertices
			for (int i = 4; i <= 8; i += 2) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(instance.getImageWidth() + 1);
				values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = r.nextInt(instance.getImageHeight() + 1);
			}
		}
	}

	// computes the fitness
	// translate triangles to images
	// and compare
	public void evaluate() {
		BufferedImage generatedImage = createImage();

		setSolutionImage(generatedImage);

		int[] generatedPixels = new int[generatedImage.getWidth() * generatedImage.getHeight()];
		PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, generatedImage.getWidth(), generatedImage.getHeight(),
				generatedPixels, 0, generatedImage.getWidth());
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int[] targetPixels = instance.getTargetPixels();

		long sum = 0;
		for (int i = 0; i < targetPixels.length; i++) {
			int c1 = targetPixels[i];
			int c2 = generatedPixels[i];
			int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
			// erro do vermelho
			int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
			// erro de verde
			int blue = (c1 & 0xff) - (c2 & 0xff);
			// erro de azul
			sum += red * red + green * green + blue * blue;
		}
		// sqrt of sum of square difference of the colors
		fitness = Math.sqrt(sum);
		shared_fitness = fitness;
	}

	// baseline mutation
	public Solution applyMutation(int nTriangles) {

		Solution temp = this.copy();
		for (int i = 0; i < nTriangles; i++) {
			// choose random triangle
			int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
			// choose random value
			int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
			if (valueIndex < 4) { // is color
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
			} else {
				if (valueIndex % 2 == 0) { // position
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageWidth() + 1);
				} else {
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageHeight() + 1);
				}
			}
		}
		return temp;
	}

	public Solution applySofterMutation(int hsbValue, int positionValue, int nTriangles) {

		Solution temp = this.copy();

		for (int i = 0; i < nTriangles; i++) {
			// choose random triangle
			int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
			int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
			int val = temp.getValue(triangleIndex * VALUES_PER_TRIANGLE + valueIndex);

			if (valueIndex < 4) { // change color 10 values
				// temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex]
				// = r.nextInt(256);

				if (r.nextBoolean()) {
					if (val + hsbValue < 255) {
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val + hsbValue;
					} else
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val + hsbValue - 255;
				}
				// decresce 10
				else {
					if (val - hsbValue >= 0) {
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val - hsbValue;
					} else
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val - hsbValue + 255;
				}
			} else {
				// if (valueIndex % 2 == 0) { // change position 20 pixels
				if (r.nextBoolean()) {
					if (val + positionValue <= instance.getImageWidth())
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val + positionValue;
					else
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val + positionValue
						- instance.getImageWidth();

				} else {
					if (val - positionValue >= 0)
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val - positionValue;
					else
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = val - positionValue
						+ instance.getImageWidth();
				}

			}
		}
		return temp;
	}

	public int computeManhattanDistance(Solution otherSolution) {
		int distance = 0;
		for (int i = 0; i < values.length; i++) {
			distance += Math.abs(values[i] - otherSolution.values[i]);
		}
		return distance;
	}

	public double computeEuclidianDistance(Solution otherSolution){
		double sumSqrErrors=0.0;
		double error = 0.0;
		for (int i = 0; i < values.length; i++) {
			error= Math.abs(values[i] - otherSolution.values[i]);
			sumSqrErrors += error*error;
		}
		//System.out.println(sumSqrErrors);
		return Math.sqrt(sumSqrErrors);
	}

	public double computeFitnessDistance(Solution otherSolution) {
		return Math.abs(fitness - otherSolution.getFitness());
	}	

	public Solution applyColorMutation() {
		Solution temp = this.copy();
		/*
		 * for every triangle in temp set value of Hue set value of Saturation
		 * set value of Brightness set value of Alpha
		 * 
		 */
		return temp;
	}

	public Solution applyPositionMutation() {
		Solution temp = this.copy();
		/*
		 * for every triangle in temp set valueXvertice1 set valueYvertice1 set
		 * valueXvertice2 set valueYvertice2 set valueXvertice3 set
		 * valueYvertice4
		 * 
		 */
		return temp;
	}

	public void draw() {
		BufferedImage generatedImage = createImage();
		Graphics g = ProblemInstance.view.getFittestDrawingView().getMainPanel().getGraphics();
		g.drawImage(generatedImage, 0, 0, ProblemInstance.view.getFittestDrawingView());
	}

	public void print() {
		System.out.printf("Fitness: %.1f\n", fitness);
	}

	public int getValue(int index) {
		return values[index];
	}

	public void setValue(int index, int value) {
		values[index] = value;
	}

	public int getHue(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 0];
	}

	public int getSaturation(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 1];
	}

	public int getBrightness(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 2];
	}

	public int getAlpha(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 3];
	}

	public int getXFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 4];
	}

	public int getYFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 5];
	}

	public int getXFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 6];
	}

	public int getYFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 7];
	}

	public int getXFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 8];
	}

	public int getYFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 9];
	}

	public void setHue(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 0] = value;
	}

	public void setSaturation(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 1] = value;
	}

	public void setBrightness(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 2] = value;
	}

	public void setAlpha(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 3] = value;
	}

	public void setXFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 4] = value;
	}

	public void setYFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 5] = value;
	}

	public void setXFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 6] = value;
	}

	public void setYFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 7] = value;
	}

	public void setXFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 8] = value;
	}

	public void setYFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 9] = value;
	}

	public int[] getVertex1(int triangleIndex) {
		return new int[] { getXFromVertex1(triangleIndex), getYFromVertex1(triangleIndex) };
	}

	public int[] getVertex2(int triangleIndex) {
		return new int[] { getXFromVertex2(triangleIndex), getYFromVertex2(triangleIndex) };
	}

	public int[] getVertex3(int triangleIndex) {
		return new int[] { getXFromVertex3(triangleIndex), getYFromVertex3(triangleIndex) };
	}

	public ProblemInstance getInstance() {
		return instance;
	}

	public int[] getValues() {
		return values;
	}

	public double getFitness() {
		return fitness;
	}

	public double getSharedFitness() {
		return shared_fitness;
	}

	public Solution copy() {
		Solution temp = new Solution(instance);
		for (int i = 0; i < values.length; i++) {
			temp.values[i] = values[i];
		}
		temp.fitness = fitness;
		return temp;
	}

	private BufferedImage createImage() {
		BufferedImage target = instance.getTargetImage();
		BufferedImage generatedImage = new BufferedImage(target.getWidth(), target.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics generatedGraphics = generatedImage.getGraphics();

		generatedGraphics.setColor(Color.GRAY);
		generatedGraphics.fillRect(0, 0, generatedImage.getWidth(), generatedImage.getHeight());
		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			generatedGraphics.setColor(expressColor(triangleIndex));
			generatedGraphics.fillPolygon(expressPolygon(triangleIndex));
		}
		return generatedImage;
	}

	private Color expressColor(int triangleIndex) {
		int hue = getHue(triangleIndex);
		int saturation = getSaturation(triangleIndex);
		int brightness = getBrightness(triangleIndex);
		int alpha = getAlpha(triangleIndex);
		Color c = Color.getHSBColor(hue / 255.0f, saturation / 255.0f, brightness / 255.0f);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private Polygon expressPolygon(int triangleIndex) {
		int[] xs = new int[] { getXFromVertex1(triangleIndex), getXFromVertex2(triangleIndex),
				getXFromVertex3(triangleIndex) };
		int[] ys = new int[] { getYFromVertex1(triangleIndex), getYFromVertex2(triangleIndex),
				getYFromVertex3(triangleIndex) };
		return new Polygon(xs, ys, 3);
	}

	public BufferedImage getSolutionImage() {
		return solutionImage;
	}

	public void setSolutionImage(BufferedImage solutionImage) {
		this.solutionImage = solutionImage;
	}

	public void setSharingFitness(double newFitness) {
		shared_fitness = newFitness;
	}

}
