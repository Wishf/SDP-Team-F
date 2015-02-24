package sdp.sdp9.vision.gui.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sdp.sdp9.vision.PitchConstants;
import sdp.sdp9.vision.PixelInfo;
import sdp.sdp9.vision.gui.GUITool;
import sdp.sdp9.vision.gui.InvertibleRangeSlider;
import sdp.sdp9.vision.gui.VisionGUI;
import sdp.sdp9.vision.interfaces.ObjectRecogniser;
import sdp.sdp9.world.StaticWorldState;

public class HistogramTool implements GUITool, ObjectRecogniser {
	private static final String[] CHANNEL_NAMES = { "Red", "Green", "Blue",
			"Hue", "Saturation", "Brightness" };

	/**
	 * Maximum values for colour channels. RGB channels range from 0 to 255. HSV
	 * channels range from 0 to 1.
	 */
	private static final float[] CHANNEL_VALUE_DIVIDERS = { 255, 255, 255, 1,
			1, 1, };

	private VisionGUI gui;
	private PitchConstants pitchConstants;

	private JFrame subWindow;
	private JList objectList;
	private GUIMouseListener mouseListener = new GUIMouseListener();
	private int currentObject = -1;

	/**
	 * When silentGUIChange is true, UI changes will not be represented in
	 * PitchConstants object.
	 */
	private boolean silentGUIChange = false;

	/**
	 * Specifies whether this tool is currently selected.
	 */
	private boolean isActive = false;

	/**
	 * Centre point of the measuring circle. If null, the circle will not be
	 * drawn.
	 */
	private Point centerPoint;

	/**
	 * Radius of the measuring circle.
	 */
	private int radius;

	/**
	 * Specifies whether histograms need to be refreshed in the next frame
	 * processing.
	 */
	private boolean needsRefresh = true;

	private HistogramWithSlider[] histograms = new HistogramWithSlider[6];

	private Observer pitchConstantsChangeObserver = new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			updateSliders();
		}
	};

	public HistogramTool(VisionGUI gui, PitchConstants pitchConstants) {
		this.gui = gui;
		this.pitchConstants = pitchConstants;
		pitchConstants.addObserver(pitchConstantsChangeObserver);

		subWindow = new JFrame("Histogram");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		subWindow.getContentPane().setLayout(
				new BoxLayout(subWindow.getContentPane(), BoxLayout.X_AXIS));

		objectList = new JList(PitchConstants.OBJECT_NAMES);
		objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		subWindow.getContentPane().add(new JScrollPane(objectList));

		subWindow.getContentPane().add(Box.createHorizontalStrut(8));

		final JPanel channelPanel = new JPanel(new GridLayout(2, 3, 8, 8));
		for (int i = 0; i < 6; i++) {
			HistogramDisplay hd = new HistogramDisplay();
			InvertibleRangeSlider irs = new InvertibleRangeSlider(0, 257);
			histograms[i] = new HistogramWithSlider(hd, irs, CHANNEL_NAMES[i]);
			channelPanel.add(histograms[i]);
		}
		registerChangeListeners();
		updateSliders();
		channelPanel.setVisible(false);
		subWindow.getContentPane().add(channelPanel);

		objectList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				currentObject = objectList.getSelectedIndex();
				if (currentObject != -1) {
					HistogramTool.this.pitchConstants.setDebugMode(
							currentObject, true, false);
				} else {
					HistogramTool.this.pitchConstants.setDebugMode(0, false,
							false);
				}
				channelPanel.setVisible(currentObject != -1);
				updateSliders();
				subWindow.pack();
			}
		});
	}

	private void updateSliders() {
		if (currentObject == -1)
			return;

		/*
		 * silentGUIChange is a temporary workaround that prevents
		 * PitchConstants data corruption. slider.setValues() fires a callback
		 * that sets PitchConstants.thresholdInverted to the state of UI, which
		 * is not updated yet.
		 */
		silentGUIChange = true;
		for (int channel = 0; channel < PitchConstants.NUM_CHANNELS; channel++) {
			InvertibleRangeSlider s = histograms[channel].slider;
			s.setValues(
					(int) (255 * pitchConstants.getLowerThreshold(
							currentObject, channel) / CHANNEL_VALUE_DIVIDERS[channel]),
					(int) (255 * pitchConstants.getUpperThreshold(
							currentObject, channel) / CHANNEL_VALUE_DIVIDERS[channel]));
			s.setInverted(pitchConstants.isThresholdInverted(currentObject,
					channel));
		}
		silentGUIChange = false;

		for (int channel = 0; channel < PitchConstants.NUM_CHANNELS; channel++) {
			histograms[channel].histogramDisplay
					.setHighlight(
							(int) (255 * pitchConstants.getLowerThreshold(
									currentObject, channel) / CHANNEL_VALUE_DIVIDERS[channel]),
							(int) (255 * pitchConstants.getUpperThreshold(
									currentObject, channel) / CHANNEL_VALUE_DIVIDERS[channel]),
							pitchConstants.isThresholdInverted(currentObject,
									channel));
		}
	}

	private void registerChangeListeners() {
		for (int channel = 0; channel < PitchConstants.NUM_CHANNELS; channel++) {
			final int loopChannel = channel;
			histograms[channel].slider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (currentObject != -1 && !silentGUIChange) {
						pitchConstants.setThresholds(
								currentObject,
								loopChannel,
								CHANNEL_VALUE_DIVIDERS[loopChannel]
										* histograms[loopChannel].slider
												.getLowerValue() / 255f,
								CHANNEL_VALUE_DIVIDERS[loopChannel]
										* histograms[loopChannel].slider
												.getUpperValue() / 255f);
						pitchConstants.setThresholdInverted(currentObject,
								loopChannel,
								histograms[loopChannel].slider.isInverted());
					}
				}
			});
		}
	}

	@Override
	public void activate() {
		isActive = true;

		gui.getVideoDisplay().addMouseListener(mouseListener);
		gui.getVideoDisplay().addMouseMotionListener(mouseListener);

		/*
		 * Position the tool window below the main GUI window.
		 */
		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x, mainWindowBounds.y
				+ mainWindowBounds.height);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		isActive = false;
		gui.getVideoDisplay().removeMouseListener(mouseListener);
		gui.getVideoDisplay().removeMouseMotionListener(mouseListener);
		subWindow.setVisible(false);
		return true;
	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}

	/**
	 * Draw the measuring circle and refresh the histograms (if necessary)
	 */
	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay,
			StaticWorldState staticWorldState) {
		if (needsRefresh) {
			needsRefresh = false;
			BufferedImage[] histogramImages = refreshHistogram(frame);
			for (int i = 0; i < 6; i++)
				histograms[i].histogramDisplay.updateImage(histogramImages[i]);
		}

		if (isActive && centerPoint != null) {
			debugGraphics.setColor(Color.WHITE);
			debugGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			debugGraphics.drawOval(centerPoint.x - radius, centerPoint.y
					- radius, 2 * radius, 2 * radius);
		}
	}

	/**
	 * Given a raw frame, creates a histogram for each channel and returns a
	 * list of them.
	 */
	private BufferedImage[] refreshHistogram(BufferedImage frame) {
		BufferedImage[] result = new BufferedImage[6];
		for (int i = 0; i < 6; i++) {
			result[i] = new BufferedImage(HistogramDisplay.HISTOGRAM_WIDTH,
					HistogramDisplay.HISTOGRAM_HEIGHT,
					BufferedImage.TYPE_3BYTE_BGR);
		}
		if (centerPoint == null)
			return result;

		int[][] valueCounts = new int[6][256];

		Rectangle selection = new Rectangle(centerPoint.x - radius,
				centerPoint.y - radius, 2 * radius, 2 * radius);
		selection = selection.intersection(new Rectangle(0, 0,
				frame.getWidth(), frame.getHeight()));

		// Gather data
		Raster raster = frame.getData();
		int[] rgb = new int[3]; // Preallocated array
		float[] hsb = new float[3];
		for (int y = (int) selection.getMinY(); y < selection.getMaxY(); y++) {
			for (int x = (int) selection.getMinX(); x < selection.getMaxX(); x++) {
				if (Math.hypot(x - centerPoint.x, y - centerPoint.y) < radius) {
					// The pixel is inside the circle
					raster.getPixel(x, y, rgb);
					Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
					// RGB processing
					for (int channel = 0; channel < 3; channel++) {
						valueCounts[channel][rgb[channel]]++;
					}
					// HSV processing
					for (int channel = 0; channel < 3; channel++) {
						valueCounts[channel + 3][(int) (255 * hsb[channel])]++;
					}
				}
			}
		}

		// Normalise values
		int maxValue = 1; // 1 to avoid division by zero
		for (int channel = 0; channel < 6; channel++) {
			for (int i = 0; i < 256; i++)
				if (valueCounts[channel][i] > maxValue)
					maxValue = valueCounts[channel][i];
		}
		double scalingFactor = 1.0 * HistogramDisplay.HISTOGRAM_HEIGHT
				/ maxValue;
		for (int channel = 0; channel < 6; channel++) {
			for (int i = 0; i < 256; i++)
				valueCounts[channel][i] = (int) (HistogramDisplay.HISTOGRAM_HEIGHT - valueCounts[channel][i]
						* scalingFactor);
		}

		// Draw the curves
		int[] xPoints = new int[256];
		for (int i = 0; i < 256; i++) {
			xPoints[i] = i * HistogramDisplay.HISTOGRAM_WIDTH / 256;
		}

		final Color[] colors = new Color[] { Color.RED, Color.GREEN,
				Color.BLUE, Color.YELLOW, // hue
				Color.GRAY, // saturation
				Color.WHITE, // brightness
		};
		for (int channel = 0; channel < 6; channel++) {
			Graphics2D g = result[channel].createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(colors[channel]);
			g.drawPolyline(xPoints, valueCounts[channel], xPoints.length);
		}
		return result;
	}

	private class GUIMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			centerPoint = e.getPoint();
			radius = 0;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			radius = (int) (Math.hypot(e.getX() - centerPoint.x, e.getY()
					- centerPoint.y) / 2);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			needsRefresh = true;
		}
	}

	/**
	 * Positions histogram above slider
	 */
	@SuppressWarnings("serial")
	private class HistogramWithSlider extends JPanel {
		private HistogramDisplay histogramDisplay;
		private InvertibleRangeSlider slider;

		public HistogramWithSlider(HistogramDisplay histogramDisplay,
				InvertibleRangeSlider slider, String label) {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(histogramDisplay);
			add(Box.createVerticalStrut(8));
			add(new JLabel(label));
			add(slider);
			this.histogramDisplay = histogramDisplay;
			this.slider = slider;
		}
	}

	@SuppressWarnings("serial")
	private class HistogramDisplay extends JPanel {
		public static final int HISTOGRAM_WIDTH = 280;
		public static final int HISTOGRAM_HEIGHT = 120;

		private BufferedImage histogramImage;
		private int highlightLeft, highlightRight;
		private boolean highlightInvert;

		private JPanel imagePanel = new JPanel() {
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				g.drawImage(histogramImage, 0, 0, null);
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.3f));
				g2d.setColor(Color.WHITE);
				if (highlightInvert) {
					g2d.fillRect(0, 0, highlightLeft, getHeight());
					g2d.fillRect(highlightRight, 0,
							getWidth() - highlightRight, getHeight());
				} else {
					g2d.fillRect(highlightLeft, 0, highlightRight
							- highlightLeft, getHeight());
				}
				g2d.dispose();
			};
		};

		public HistogramDisplay() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			imagePanel.setBackground(Color.BLACK);
			imagePanel.setPreferredSize(new Dimension(HISTOGRAM_WIDTH,
					HISTOGRAM_HEIGHT));
			add(imagePanel);
		}

		public void updateImage(BufferedImage image) {
			histogramImage = image;
			imagePanel.repaint();
		}

		public void setHighlight(int left, int right, boolean invert) {
			highlightLeft = HISTOGRAM_WIDTH * left / 256;
			highlightRight = HISTOGRAM_WIDTH * right / 256;
			highlightInvert = invert;
			imagePanel.repaint();
		}
	}
}
