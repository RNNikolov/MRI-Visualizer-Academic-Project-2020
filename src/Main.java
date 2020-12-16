
/**
 * Assignment 1 - CS-255
 * 
 * @author Radoslav Nikolaev Nikolov 
 * Student number: 974054
 * 
 * @version 1.0
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import java.io.*;

public class Main extends Application {
	short cthead[][][]; // store the 3D volume data set
	short min, max; // min/max value in the 3D volume data set

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");

		ReadData();

		int width = 256;
		int height = 256;
		int i, j, k, h, n;

		double col;
		short datum;

		// Creating the histogram and histogram equalization
		double[] cd = new double[256 * 256 * 113]; // cumulative distribution
		double[] mapping = new double[256 * 256 * 113];
		int[] histogram = new int[max - min + 1]; // 3366 values for the current data set
		int index;

		WritableImage histogram_image = new WritableImage(width, height);
		ImageView histogramView = new ImageView(histogram_image);
		PixelWriter image_writer = histogram_image.getPixelWriter();

		for (h = 0; h < (max - min + 1); h++) {
			histogram[h] = 0;

		}

		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					index = cthead[k][j][i] - min;
					histogram[index]++;
				}
			}
		}

		cd[0] = histogram[0];
		for (n = 1; n < max - min + 1; n++) {
			cd[n] = cd[n - 1] + histogram[n];
			mapping[n] = 255.0 * (cd[n] / cd.length);

		}

		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {

					datum = cthead[k][j][i];

					col = mapping[datum - min] / 255.0;

					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}

		}

		// Creating the line chart and putting the data on it
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();

		final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setTitle("Histrogram");
		lineChart.setMaxSize(800, 500);
		lineChart.setCreateSymbols(false);

		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

		for (h = 0; h < max - min + 1; h++) {
			series.getData().add(new XYChart.Data<Number, Number>(h, mapping[h]));
		}

		// Creating Writable Images and Image Views
		WritableImage medical_image = new WritableImage(width, height);
		ImageView imageView = new ImageView(medical_image);

		int height2 = 113;
		WritableImage medical_image2 = new WritableImage(width, height2);
		ImageView imageView2 = new ImageView(medical_image2);

		WritableImage medical_image3 = new WritableImage(width, height2);
		ImageView imageView3 = new ImageView(medical_image3);

		ImageView imageView4 = new ImageView();

		// Creating the ScrollPane and HBox
		ScrollPane thumbnail = new ScrollPane();
		thumbnail.setPrefSize(1300, 150);
		thumbnail.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox box = new HBox();

		// Creating buttons
		Button thumbnail_button = new Button("Thumbnail");

		Button histogram_button = new Button("Histogram");

		Button mip_button = new Button("MIP");

		// Creating sliders for the different views and resizing
		Slider zslider = new Slider(0, 112, 0);
		Slider yslider = new Slider(0, 255, 0);
		Slider xslider = new Slider(0, 255, 0);
		Slider resize_slider = new Slider(20, 500, 20);

		resize_slider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				imageView.setImage(resize(medical_image, newValue.intValue()));
				imageView2.setImage(resize(medical_image2, newValue.intValue()));
				imageView3.setImage(resize(medical_image3, newValue.intValue()));
			}
		});

		zslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				MIP2(medical_image, newValue.intValue());
				imageView.setImage(medical_image);
			}
		});

		yslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				MIP3(medical_image2, newValue.intValue());
				imageView2.setImage(medical_image2);
			}
		});

		xslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				MIP4(medical_image3, newValue.intValue());
				imageView3.setImage(medical_image3);
			}
		});

		//Javafx part. Creating a BorderPane, VBox and Gridpane which I put inside the BorderPane.
		BorderPane root = new BorderPane();

		VBox vertical = new VBox();
		vertical.setSpacing(10);
		VBox.setMargin(mip_button, new Insets(0, 10, 0, 10));
		VBox.setMargin(thumbnail_button, new Insets(0, 10, 0, 10));
		VBox.setMargin(histogram_button, new Insets(0, 10, 0, 10));
		VBox.setMargin(xslider, new Insets(0, 10, 0, 10));
		VBox.setMargin(yslider, new Insets(0, 10, 0, 10));
		VBox.setMargin(resize_slider, new Insets(0, 10, 0, 10));
		VBox.setMargin(zslider, new Insets(10, 10, 0, 10));

		GridPane center = new GridPane();
		center.setHgap(10);
		center.setVgap(10);
		center.setPadding(new Insets(0, 10, 0, 10));

		vertical.getChildren().addAll(zslider, yslider, xslider, mip_button, thumbnail_button, histogram_button,
				resize_slider);

		lineChart.getData().add(series);

		root.setLeft(vertical);
		root.setTop(thumbnail);
		root.setCenter(center);
		
        //Functionality of the 3 buttons
		histogram_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				center.getChildren().removeAll(histogramView, imageView4, imageView, imageView2, imageView3);
				thumbnail.setContent(null);
				root.setBottom(lineChart);
				center.add(histogramView, 0, 0);
			}
		});

		mip_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				center.getChildren().removeAll(histogramView, imageView4);
				zslider.setValue(0);
				yslider.setValue(0);
				xslider.setValue(0);
				thumbnail.setContent(null);
				root.setBottom(null);
				center.add(imageView, 0, 0);
				center.add(imageView2, 1, 0);
				center.add(imageView3, 2, 0);
				MIP(medical_image, medical_image2, medical_image3);
			}
		});

		thumbnail_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				center.getChildren().removeAll(histogramView, imageView, imageView2, imageView3);
				root.setBottom(null);
				imageView4.setImage(null);
				center.add(imageView4, 0, 0);
				for (WritableImage item : Thumbnail()) {

					ImageView image_view = new ImageView(item);
					image_view.setOnMouseClicked(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent event) {
							imageView4.setImage(resize(item, 500));
						}
					});
					box.getChildren().add(image_view);
				}
				thumbnail.setContent(box);

			}
		});

		Scene scene = new Scene(root, 1800, 950);
		stage.setScene(scene);
		stage.show();
	}

	// Function to read in the cthead data set
	public void ReadData() throws IOException {
		// File name is hard coded here - much nicer to have a dialog to select it and
		// capture the size from the user
		File file = new File("CThead.raw");
		// Read the data quickly via a buffer (in C++ you can just do a single fread - I
		// couldn't find if there is an equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; // loop through the 3D data set

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE; // set to extreme values
		short read; // value read in
		int b1, b2; // data is wrong Endian (check Wikipedia) for Java so we need to swap the bytes
					// around

		cthead = new short[113][256][256]; // allocate the memory - note this is fixed for this data set
		// loop through the data reading it in
		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					// because the Endianess is wrong, it needs to be read byte at a time and
					// swapped
					b1 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					b2 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					read = (short) ((b2 << 8) | b1); // and swizzle the bytes around
					if (read < min)
						min = read; // update the minimum
					if (read > max)
						max = read; // update the maximum
					cthead[k][j][i] = read; // put the short into memory (in C++ you can replace all this code with one
											// fread)
				}
			}
		}

		System.out.println(min + " " + max); // diagnostic - for CThead this should be -1117, 2248
		// (i.e. there are 3366 levels of grey (we are trying to display on 256 levels
		// of grey)
		// therefore histogram equalization would be a good thing
	}

	/**
	 * Creating thumbnail of the images from the data set.
	 * 
	 * @return ArrayList of Writable images.
	 */
	public ArrayList<WritableImage> Thumbnail() {

		ArrayList<WritableImage> thumbnail = new ArrayList<WritableImage>();
		WritableImage image = new WritableImage(256, 256);
		int w = (int) image.getWidth(), h = (int) image.getHeight();
		PixelWriter image_writer = image.getPixelWriter();

		float datum, col;

		for (int slice = 0; slice < 113; slice++) {
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {

					datum = cthead[slice][j][i];

					col = (((float) datum - (float) min) / ((float) (max - min)));

					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

				}

			}
			thumbnail.add(resize(image, 150));
		}
		return thumbnail;
	}

	/**
	 * Resizing an image using bilinear interpolation.
	 * 
	 * @param image the current image to be resized.
	 * @param scale new size of the image.
	 * @return the resized image.
	 */
	public WritableImage resize(WritableImage image, int scale) {

		int w = (int) image.getWidth();
		int h = (int) image.getHeight();

		WritableImage image2 = new WritableImage(scale, scale);

		PixelReader originalReader = image.getPixelReader();
		PixelWriter image2_writer = image2.getPixelWriter();

		double pixX, pixY;

		for (int x = 0; x < scale; x++) {
			pixX = (double) x / scale * (w - 1);
			for (int y = 0; y < scale; y++) {
				pixY = (double) y / scale * (h - 1);

				int x1 = (int) Math.floor(pixX);
				int y1 = (int) Math.floor(pixY);

				double a = originalReader.getColor(x1, y1).getGreen();
				double b = originalReader.getColor(x1 + 1, y1).getGreen();
				double c = originalReader.getColor(x1, y1 + 1).getGreen();
				double d = originalReader.getColor(x1 + 1, y1 + 1).getGreen();

				double deltaX = pixX - x1;
				double deltaY = pixY - y1;

				double top = a * (1 - deltaX) + (b * deltaX);
				double bottom = c * (1 - deltaX) + (d * deltaX);
				double middle = top * (1 - deltaY) + (bottom * deltaY);

				image2_writer.setColor(x, y, Color.color(middle, middle, middle, 1.0));

			}
		}

		return image2;
	}

	/**
	 * Implementing maximum intensity projection of the three views.
	 * 
	 * @param image
	 * @param image2
	 * @param image3
	 */
	public void MIP(WritableImage image, WritableImage image2, WritableImage image3) {

		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, k;
		PixelWriter image_writer = image.getPixelWriter();

		int w2 = (int) image2.getWidth(), h2 = (int) image2.getHeight(), i2, j2, k2;
		PixelWriter image_writer2 = image2.getPixelWriter();

		int w3 = (int) image3.getWidth(), h3 = (int) image3.getHeight(), i3, j3, k3;
		PixelWriter image_writer3 = image3.getPixelWriter();

		float col;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {

				short maximum = Short.MIN_VALUE;

				for (k = 0; k < 113; k++) {

					maximum = (short) Math.max(cthead[k][j][i], maximum);
				}

				col = (((float) maximum - (float) min) / ((float) (max - min)));

				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

			}
		}

		for (j2 = 0; j2 < h2; j2++) {
			for (i2 = 0; i2 < w2; i2++) {

				short maximum = Short.MIN_VALUE;

				for (k2 = 0; k2 < 255; k2++) {

					maximum = (short) Math.max(cthead[j2][k2][i2], maximum);
				}

				col = (((float) maximum - (float) min) / ((float) (max - min)));

				image_writer2.setColor(i2, j2, Color.color(col, col, col, 1.0));

			}
		}

		for (j3 = 0; j3 < h3; j3++) {
			for (i3 = 0; i3 < w3; i3++) {

				short maximum = Short.MIN_VALUE;

				for (k3 = 0; k3 < 255; k3++) {

					maximum = (short) Math.max(cthead[j3][i3][k3], maximum);
				}

				col = (((float) maximum - (float) min) / ((float) (max - min)));

				image_writer3.setColor(i3, j3, Color.color(col, col, col, 1.0));

			}
		}

	}

	/**
	 * Creating the top-bottom view of the data set.
	 * 
	 * @param image
	 * @param slice number of the image in the data set.
	 */
	public void MIP2(WritableImage image, int slice) {

		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {

				datum = cthead[slice][j][i];

				col = (((float) datum - (float) min) / ((float) (max - min)));

				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

			}
		}
	}

	/**
	 * Creating the front view of the data set.
	 * 
	 * @param image
	 * @param slice number of the image in the data set.
	 */
	public void MIP3(WritableImage image, int slice) {

		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {

				datum = cthead[j][slice][i];

				col = (((float) datum - (float) min) / ((float) (max - min)));

				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

			}
		}
	}

	/**
	 * Creating the side view of the data set.
	 * 
	 * @param image
	 * @param slice number of the image in the data set.
	 */
	public void MIP4(WritableImage image, int slice) {

		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {

				datum = cthead[j][i][slice];

				col = (((float) datum - (float) min) / ((float) (max - min)));

				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

			}
		}
	}

	public static void main(String[] args) {

		launch();
	}

}