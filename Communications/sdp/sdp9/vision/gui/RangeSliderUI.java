package sdp.sdp9.vision.gui;
// FROM: https://github.com/ernieyu/Swing-range-slider
/* The MIT License

Copyright (c) 2010 Ernest Yu. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE. */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

/**
* UI delegate for the RangeSlider component. RangeSliderUI paints two thumbs,
* one for the lower value and one for the upper value.
*/
class RangeSliderUI extends BasicSliderUI {
	/** Color of selected range. */
	private Color rangeColor = Color.GREEN;

	/** Location and size of thumb for upper value. */
	private Rectangle upperThumbRect;
	/** Indicator that determines whether upper thumb is selected. */
	private boolean upperThumbSelected;

	/** Indicator that determines whether lower thumb is being dragged. */
	private transient boolean lowerDragging;
	/** Indicator that determines whether upper thumb is being dragged. */
	private transient boolean upperDragging;

	/**
	 * Constructs a RangeSliderUI for the specified slider component.
	 * @param b RangeSlider
	 */
	public RangeSliderUI(RangeSlider b) {
		super(b);
	}

	/**
	 * Installs this UI delegate on the specified component.
	 */
	@Override
	public void installUI(JComponent c) {
		this.upperThumbRect = new Rectangle();
		super.installUI(c);
	}

	/**
	 * Creates a listener to handle track events in the specified slider.
	 */
	@Override
	protected TrackListener createTrackListener(JSlider slider) {
		return new RangeTrackListener();
	}

	/**
	 * Creates a listener to handle change events in the specified slider.
	 */
	@Override
	protected ChangeListener createChangeListener(JSlider slider) {
		return new ChangeHandler();
	}

	/**
	 * Updates the dimensions for both thumbs.
	 */
	@Override
	protected void calculateThumbSize() {
		// Call superclass method for lower thumb size.
		super.calculateThumbSize();

		// Set upper thumb size.
		this.upperThumbRect.setSize(this.thumbRect.width, this.thumbRect.height);
	}

	/**
	 * Updates the locations for both thumbs.
	 */
	@Override
	protected void calculateThumbLocation() {
		// Call superclass method for lower thumb location.
		super.calculateThumbLocation();

		// Adjust upper value to snap to ticks if necessary.
		if (this.slider.getSnapToTicks()) {
			int upperValue = this.slider.getValue() + this.slider.getExtent();
			int snappedValue = upperValue;
			int majorTickSpacing = this.slider.getMajorTickSpacing();
			int minorTickSpacing = this.slider.getMinorTickSpacing();
			int tickSpacing = 0;

			if (minorTickSpacing > 0) {
				tickSpacing = minorTickSpacing;
			} else if (majorTickSpacing > 0) {
				tickSpacing = majorTickSpacing;
			}

			if (tickSpacing != 0) {
				// If it's not on a tick, change the value
				if ((upperValue - this.slider.getMinimum()) % tickSpacing != 0) {
					float temp = (float)(upperValue - this.slider.getMinimum()) / (float)tickSpacing;
					int whichTick = Math.round(temp);
					snappedValue = this.slider.getMinimum() + (whichTick * tickSpacing);
				}

				if (snappedValue != upperValue) {
					this.slider.setExtent(snappedValue - this.slider.getValue());
				}
			}
		}

		// Calculate upper thumb location. The thumb is centered over its
		// value on the track.
		if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
			int upperPosition = xPositionForValue(this.slider.getValue() + this.slider.getExtent());
			this.upperThumbRect.x = upperPosition - (this.upperThumbRect.width / 2);
			this.upperThumbRect.y = this.trackRect.y;

		} else {
			int upperPosition = yPositionForValue(this.slider.getValue() + this.slider.getExtent());
			this.upperThumbRect.x = this.trackRect.x;
			this.upperThumbRect.y = upperPosition - (this.upperThumbRect.height / 2);
		}
	}

	/**
	 * Returns the size of a thumb.
	 */
	@Override
	protected Dimension getThumbSize() {
		return new Dimension(12, 12);
	}

	/**
	 * Paints the slider. The selected thumb is always painted on top of the
	 * other thumb.
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Rectangle clipRect = g.getClipBounds();
		if (this.upperThumbSelected) {
			// Paint lower thumb first, then upper thumb.
			if (clipRect.intersects(this.thumbRect)) {
				paintLowerThumb(g);
			}
			if (clipRect.intersects(this.upperThumbRect)) {
				paintUpperThumb(g);
			}

		} else {
			// Paint upper thumb first, then lower thumb.
			if (clipRect.intersects(this.upperThumbRect)) {
				paintUpperThumb(g);
			}
			if (clipRect.intersects(this.thumbRect)) {
				paintLowerThumb(g);
			}
		}
	}

	/**
	 * Paints the track.
	 */
	@Override
	public void paintTrack(Graphics g) {
		// Draw track.
		super.paintTrack(g);

		Rectangle trackBounds = this.trackRect;

		if (this.slider.getOrientation() == JSlider.HORIZONTAL) {
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			int lowerX = this.thumbRect.x + (this.thumbRect.width / 2);
			int upperX = this.upperThumbRect.x + (this.upperThumbRect.width / 2);

			// Determine track position.
			int cy = (trackBounds.height / 2) - 2;

			// Save color and shift position.
			Color oldColor = g.getColor();
			g.translate(trackBounds.x, trackBounds.y + cy);

			// Draw selected range.
			g.setColor(this.rangeColor);
			for (int y = 0; y <= 3; y++) {
				g.drawLine(lowerX - trackBounds.x, y, upperX - trackBounds.x, y);
			}

			// Restore position and color.
			g.translate(-trackBounds.x, -(trackBounds.y + cy));
			g.setColor(oldColor);

		} else {
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			int lowerY = this.thumbRect.x + (this.thumbRect.width / 2);
			int upperY = this.upperThumbRect.x + (this.upperThumbRect.width / 2);

			// Determine track position.
			int cx = (trackBounds.width / 2) - 2;

			// Save color and shift position.
			Color oldColor = g.getColor();
			g.translate(trackBounds.x + cx, trackBounds.y);

			// Draw selected range.
			g.setColor(this.rangeColor);
			for (int x = 0; x <= 3; x++) {
				g.drawLine(x, lowerY - trackBounds.y, x, upperY - trackBounds.y);
			}

			// Restore position and color.
			g.translate(-(trackBounds.x + cx), -trackBounds.y);
			g.setColor(oldColor);
		}
	}

	/**
	 * Overrides superclass method to do nothing. Thumb painting is handled
	 * within the <code>paint()</code> method.
	 */
	@Override
	public void paintThumb(Graphics g) {
		// Do nothing.
	}

	/**
	 * Paints the thumb for the lower value using the specified graphics object.
	 */
	private void paintLowerThumb(Graphics g) {
		Rectangle knobBounds = this.thumbRect;
		int w = knobBounds.width;
		int h = knobBounds.height;

		// Create graphics copy.
		Graphics2D g2d = (Graphics2D) g.create();

		// Create default thumb shape.
		Shape thumbShape = createThumbShape(w - 1, h - 1);

		// Draw thumb.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(knobBounds.x, knobBounds.y);

		g2d.setColor(Color.CYAN);
		g2d.fill(thumbShape);

		g2d.setColor(Color.BLUE);
		g2d.draw(thumbShape);

		// Dispose graphics.
		g2d.dispose();
	}

	/**
	 * Paints the thumb for the upper value using the specified graphics object.
	 */
	private void paintUpperThumb(Graphics g) {
		Rectangle knobBounds = this.upperThumbRect;
		int w = knobBounds.width;
		int h = knobBounds.height;

		// Create graphics copy.
		Graphics2D g2d = (Graphics2D) g.create();

		// Create default thumb shape.
		Shape thumbShape = createThumbShape(w - 1, h - 1);

		// Draw thumb.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(knobBounds.x, knobBounds.y);

		g2d.setColor(Color.PINK);
		g2d.fill(thumbShape);

		g2d.setColor(Color.RED);
		g2d.draw(thumbShape);

		// Dispose graphics.
		g2d.dispose();
	}

	/**
	 * Returns a Shape representing a thumb.
	 */
	private static Shape createThumbShape(int width, int height) {
		// Use circular shape.
		Ellipse2D shape = new Ellipse2D.Double(0, 0, width, height);
		return shape;
	}

	/**
	 * Sets the location of the upper thumb, and repaints the slider. This is
	 * called when the upper thumb is dragged to repaint the slider. The
	 * <code>setThumbLocation()</code> method performs the same task for the
	 * lower thumb.
	 */
	private void setUpperThumbLocation(int x, int y) {
		Rectangle upperUnionRect = new Rectangle();
		upperUnionRect.setBounds(this.upperThumbRect);

		this.upperThumbRect.setLocation(x, y);

		SwingUtilities.computeUnion(this.upperThumbRect.x, this.upperThumbRect.y, this.upperThumbRect.width, this.upperThumbRect.height, upperUnionRect);
		this.slider.repaint(upperUnionRect.x, upperUnionRect.y, upperUnionRect.width, upperUnionRect.height);
	}

	/**
	 * Moves the selected thumb in the specified direction by a block increment.
	 * This method is called when the user presses the Page Up or Down keys.
	 */
	public void scrollByBlock(int direction) {
		synchronized (this.slider) {
			int blockIncrement = (this.slider.getMaximum() - this.slider.getMinimum()) / 10;
			if (blockIncrement <= 0 && this.slider.getMaximum() > this.slider.getMinimum()) {
				blockIncrement = 1;
			}
			int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

			if (this.upperThumbSelected) {
				int oldValue = ((RangeSlider) this.slider).getUpperValue();
				((RangeSlider) this.slider).setUpperValue(oldValue + delta);
			} else {
				int oldValue = this.slider.getValue();
				this.slider.setValue(oldValue + delta);
			}
		}
	}

	/**
	 * Moves the selected thumb in the specified direction by a unit increment.
	 * This method is called when the user presses one of the arrow keys.
	 */
	public void scrollByUnit(int direction) {
		synchronized (this.slider) {
			int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

			if (this.upperThumbSelected) {
				int oldValue = ((RangeSlider) this.slider).getUpperValue();
				((RangeSlider) this.slider).setUpperValue(oldValue + delta);
			} else {
				int oldValue = this.slider.getValue();
				this.slider.setValue(oldValue + delta);
			}
		}
	}

	/**
	 * Listener to handle model change events. This calculates the thumb
	 * locations and repaints the slider if the value change is not caused by
	 * dragging a thumb.
	 */
	public class ChangeHandler implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			if (!RangeSliderUI.this.lowerDragging && !RangeSliderUI.this.upperDragging) {
				calculateThumbLocation();
				RangeSliderUI.this.slider.repaint();
			}
		}
	}

	/**
	 * Listener to handle mouse movements in the slider track.
	 */
	public class RangeTrackListener extends TrackListener {

		@Override
		public void mousePressed(MouseEvent e) {
			if (!RangeSliderUI.this.slider.isEnabled()) {
				return;
			}

			this.currentMouseX = e.getX();
			this.currentMouseY = e.getY();

			if (RangeSliderUI.this.slider.isRequestFocusEnabled()) {
				RangeSliderUI.this.slider.requestFocus();
			}

			// Determine which thumb is pressed. If the upper thumb is
			// selected (last one dragged), then check its position first;
			// otherwise check the position of the lower thumb first.
			boolean lowerPressed = false;
			boolean upperPressed = false;
			if (RangeSliderUI.this.upperThumbSelected) {
				if (RangeSliderUI.this.upperThumbRect.contains(this.currentMouseX, this.currentMouseY)) {
					upperPressed = true;
				} else if (RangeSliderUI.this.thumbRect.contains(this.currentMouseX, this.currentMouseY)) {
					lowerPressed = true;
				}
			} else {
				if (RangeSliderUI.this.thumbRect.contains(this.currentMouseX, this.currentMouseY)) {
					lowerPressed = true;
				} else if (RangeSliderUI.this.upperThumbRect.contains(this.currentMouseX, this.currentMouseY)) {
					upperPressed = true;
				}
			}

			// Handle lower thumb pressed.
			if (lowerPressed) {
				switch (RangeSliderUI.this.slider.getOrientation()) {
				case JSlider.VERTICAL:
					this.offset = this.currentMouseY - RangeSliderUI.this.thumbRect.y;
					break;
				case JSlider.HORIZONTAL:
					this.offset = this.currentMouseX - RangeSliderUI.this.thumbRect.x;
					break;
				}
				RangeSliderUI.this.upperThumbSelected = false;
				RangeSliderUI.this.lowerDragging = true;
				return;
			}
			RangeSliderUI.this.lowerDragging = false;

			// Handle upper thumb pressed.
			if (upperPressed) {
				switch (RangeSliderUI.this.slider.getOrientation()) {
				case JSlider.VERTICAL:
					this.offset = this.currentMouseY - RangeSliderUI.this.upperThumbRect.y;
					break;
				case JSlider.HORIZONTAL:
					this.offset = this.currentMouseX - RangeSliderUI.this.upperThumbRect.x;
					break;
				}
				RangeSliderUI.this.upperThumbSelected = true;
				RangeSliderUI.this.upperDragging = true;
				return;
			}
			RangeSliderUI.this.upperDragging = false;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			RangeSliderUI.this.lowerDragging = false;
			RangeSliderUI.this.upperDragging = false;
			RangeSliderUI.this.slider.setValueIsAdjusting(false);
			super.mouseReleased(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (!RangeSliderUI.this.slider.isEnabled()) {
				return;
			}

			this.currentMouseX = e.getX();
			this.currentMouseY = e.getY();

			if (RangeSliderUI.this.lowerDragging) {
				RangeSliderUI.this.slider.setValueIsAdjusting(true);
				moveLowerThumb();

			} else if (RangeSliderUI.this.upperDragging) {
				RangeSliderUI.this.slider.setValueIsAdjusting(true);
				moveUpperThumb();
			}
		}

		@Override
		public boolean shouldScroll(int direction) {
			return false;
		}

		/**
		 * Moves the location of the lower thumb, and sets its corresponding
		 * value in the slider.
		 */
		 private void moveLowerThumb() {
			int thumbMiddle = 0;

			switch (RangeSliderUI.this.slider.getOrientation()) {
			case JSlider.VERTICAL:
				int halfThumbHeight = RangeSliderUI.this.thumbRect.height / 2;
				int thumbTop = this.currentMouseY - this.offset;
				int trackTop = RangeSliderUI.this.trackRect.y;
				int trackBottom = RangeSliderUI.this.trackRect.y + (RangeSliderUI.this.trackRect.height - 1);
				int vMax = yPositionForValue(RangeSliderUI.this.slider.getValue() + RangeSliderUI.this.slider.getExtent());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackBottom = vMax;
				} else {
					trackTop = vMax;
				}
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				setThumbLocation(RangeSliderUI.this.thumbRect.x, thumbTop);

				// Update slider value.
				thumbMiddle = thumbTop + halfThumbHeight;
				RangeSliderUI.this.slider.setValue(valueForYPosition(thumbMiddle));
				break;

			case JSlider.HORIZONTAL:
				int halfThumbWidth = RangeSliderUI.this.thumbRect.width / 2;
				int thumbLeft = this.currentMouseX - this.offset;
				int trackLeft = RangeSliderUI.this.trackRect.x;
				int trackRight = RangeSliderUI.this.trackRect.x + (RangeSliderUI.this.trackRect.width - 1);
				int hMax = xPositionForValue(RangeSliderUI.this.slider.getValue() + RangeSliderUI.this.slider.getExtent());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackLeft = hMax;
				} else {
					trackRight = hMax;
				}
				thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				setThumbLocation(thumbLeft, RangeSliderUI.this.thumbRect.y);

				// Update slider value.
				thumbMiddle = thumbLeft + halfThumbWidth;
				RangeSliderUI.this.slider.setValue(valueForXPosition(thumbMiddle));
				break;

			default:
				return;
			}
		 }

		 /**
		  * Moves the location of the upper thumb, and sets its corresponding
		  * value in the slider.
		  */
		 private void moveUpperThumb() {
			 int thumbMiddle = 0;

			 switch (RangeSliderUI.this.slider.getOrientation()) {
			 case JSlider.VERTICAL:
				 int halfThumbHeight = RangeSliderUI.this.thumbRect.height / 2;
				 int thumbTop = this.currentMouseY - this.offset;
				 int trackTop = RangeSliderUI.this.trackRect.y;
				 int trackBottom = RangeSliderUI.this.trackRect.y + (RangeSliderUI.this.trackRect.height - 1);
				 int vMin = yPositionForValue(RangeSliderUI.this.slider.getValue());

				 // Apply bounds to thumb position.
				 if (drawInverted()) {
					 trackTop = vMin;
				 } else {
					 trackBottom = vMin;
				 }
				 thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				 thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				 setUpperThumbLocation(RangeSliderUI.this.thumbRect.x, thumbTop);

				 // Update slider extent.
				 thumbMiddle = thumbTop + halfThumbHeight;
				 RangeSliderUI.this.slider.setExtent(valueForYPosition(thumbMiddle) - RangeSliderUI.this.slider.getValue());
				 break;

			 case JSlider.HORIZONTAL:
				 int halfThumbWidth = RangeSliderUI.this.thumbRect.width / 2;
				 int thumbLeft = this.currentMouseX - this.offset;
				 int trackLeft = RangeSliderUI.this.trackRect.x;
				 int trackRight = RangeSliderUI.this.trackRect.x + (RangeSliderUI.this.trackRect.width - 1);
				 int hMin = xPositionForValue(RangeSliderUI.this.slider.getValue());

				 // Apply bounds to thumb position.
				 if (drawInverted()) {
					 trackRight = hMin;
				 } else {
					 trackLeft = hMin;
				 }
				 thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				 thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				 setUpperThumbLocation(thumbLeft, RangeSliderUI.this.thumbRect.y);

				 // Update slider extent.
				 thumbMiddle = thumbLeft + halfThumbWidth;
				 RangeSliderUI.this.slider.setExtent(valueForXPosition(thumbMiddle) - RangeSliderUI.this.slider.getValue());
				 break;

			 default:
				 return;
			 }
		 }
	}
}

