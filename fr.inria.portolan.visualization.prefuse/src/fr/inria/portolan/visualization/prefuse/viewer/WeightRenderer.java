/*******************************************************************************
 * Copyright (c) 2010 INRIA Rennes Bretagne-Atlantique.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     INRIA Rennes Bretagne-Atlantique - initial API and implementation
 *******************************************************************************/
package fr.inria.portolan.visualization.prefuse.viewer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import prefuse.Constants;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.ImageFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StringLib;
import prefuse.visual.VisualItem;

/**
 * Clone of LabelRenderer Prefuse class, because we only need
 * to replace Image by Shape and get all the other stuff as-is...
 * 
 <h3>Third Party Content</h3>

<p>The Content includes items that have been sourced from third parties as set out below. If you 
did not receive this Content directly from the Eclipse Foundation, the following is provided 
for informational purposes only, and you should look to the Redistributor&rsquo;s license for 
terms and conditions of use.</p>

<h4>Prefuse 0.1.0</h4>

<p>
		Prefuse is:
		<blockquote>
		Copyright (c) 2004-2007 Regents of the University of California.<br/>
		All rights reserved. 
		</blockquote>
		Your use of Prefuse is subject to the terms and conditions of the Prefuse license.  A copy of the
		license is contained in the file <a href="about_files/license-prefuse.txt" target="_blank">about_files/license-prefuse.txt</a>.
		</p>

<p>
		The project information including source code, documentations and demo programs are available on
		the <a href="http://prefuse.org">Prefuse web site</a>.</p>

*
 */
public class WeightRenderer extends AbstractShapeRenderer {

    protected ImageFactory m_images = null;
    protected String m_delim = "\n";
    
    protected String m_labelName = "label";
    
    protected int m_xAlign = Constants.CENTER;
    protected int m_yAlign = Constants.CENTER;
    protected int m_hTextAlign = Constants.CENTER;
    protected int m_vTextAlign = Constants.CENTER;
    protected int m_hImageAlign = Constants.CENTER;
    protected int m_vImageAlign = Constants.CENTER;
    protected int m_imagePos = Constants.BOTTOM;
    
    protected int m_horizBorder = 2;
    protected int m_vertBorder  = 0;
    protected int m_imageMargin = 2;
    protected int m_arcWidth    = 0;
    protected int m_arcHeight   = 0;

    protected int m_maxTextWidth = -1;
    
    /** Transform used to scale and position images */
    AffineTransform m_transform = new AffineTransform();
    
    /** The holder for the currently computed bounding box */
    protected RectangularShape m_bbox  = new Rectangle2D.Double();
    protected Point2D m_pt = new Point2D.Double(); // temp point
    protected Font    m_font; // temp font holder
    protected String    m_text; // label text
    protected Dimension m_textDim = new Dimension(); // text width / height
    
    protected double _baseSize = 1;

    /**
     * Create a new LabelRenderer. By default the field "label" is used
     * as the field name for looking up text, and no image is used.
     */
    public WeightRenderer() {
    }
    
    /**
     * Create a new LabelRenderer. Draws a text label using the given
     * text data field and does not draw an image.
     * @param textField the data field for the text label.
     */
    public WeightRenderer(String textField) {
        this.setTextField(textField);
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Rounds the corners of the bounding rectangle in which the text
     * string is rendered. This will only be seen if either the stroke
     * or fill color is non-transparent.
     * @param arcWidth the width of the curved corner
     * @param arcHeight the height of the curved corner
     */
    public void setRoundedCorner(int arcWidth, int arcHeight) {
        if ( (arcWidth == 0 || arcHeight == 0) && 
            !(this.m_bbox instanceof Rectangle2D) ) {
            this.m_bbox = new Rectangle2D.Double();
        } else {
            if ( !(this.m_bbox instanceof RoundRectangle2D) )
                this.m_bbox = new RoundRectangle2D.Double();
            ((RoundRectangle2D)this.m_bbox)
                .setRoundRect(0,0,10,10,arcWidth,arcHeight);
            this.m_arcWidth = arcWidth;
            this.m_arcHeight = arcHeight;
        }
    }

    /**
     * Get the field name to use for text labels.
     * @return the data field for text labels, or null for no text
     */
    public String getTextField() {
        return this.m_labelName;
    }
    
    /**
     * Set the field name to use for text labels.
     * @param textField the data field for text labels, or null for no text
     */
    public void setTextField(String textField) {
        this.m_labelName = textField;
    }
    
    /**
     * Sets the maximum width that should be allowed of the text label.
     * A value of -1 specifies no limit (this is the default).
     * @param maxWidth the maximum width of the text or -1 for no limit
     */
    public void setMaxTextWidth(int maxWidth) {
        this.m_maxTextWidth = maxWidth;
    }
    
    /**
     * Returns the text to draw. Subclasses can override this class to
     * perform custom text selection.
     * @param item the item to represent as a <code>String</code>
     * @return a <code>String</code> to draw
     */
    protected String getText(VisualItem item) {
        String s = null;
        if ( item.canGetString(this.m_labelName) ) {
            return item.getString(this.m_labelName);            
        }
        return s;
    }

    
    // ------------------------------------------------------------------------
    // Rendering
    
    private String computeTextDimensions(VisualItem item, String text,
                                         double size)
    {
        // put item font in temp member variable
        this.m_font = item.getFont();
        // scale the font as needed
        if ( size != 1 ) {
            this.m_font = FontLib.getFont(this.m_font.getName(), this.m_font.getStyle(),
                                     size*this.m_font.getSize());
        }
        
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(this.m_font);
        StringBuffer str = null;
        
        // compute the number of lines and the maximum width
        int nlines = 1, w = 0, start = 0, end = text.indexOf(this.m_delim);
        this.m_textDim.width = 0;
        String line;
        for ( ; end >= 0; ++nlines ) {
            w = fm.stringWidth(line=text.substring(start,end));
            // abbreviate line as needed
            if ( this.m_maxTextWidth > -1 && w > this.m_maxTextWidth ) {
                if ( str == null )
                    str = new StringBuffer(text.substring(0,start));
                str.append(StringLib.abbreviate(line, fm, this.m_maxTextWidth));
                str.append(this.m_delim);
                w = this.m_maxTextWidth;
            } else if ( str != null ) {
                str.append(line).append(this.m_delim);
            }
            // update maximum width and substring indices
            this.m_textDim.width = Math.max(this.m_textDim.width, w);
            start = end+1;
            end = text.indexOf(this.m_delim, start);
        }
        w = fm.stringWidth(line=text.substring(start));
        // abbreviate line as needed
        if ( this.m_maxTextWidth > -1 && w > this.m_maxTextWidth ) {
            if ( str == null )
                str = new StringBuffer(text.substring(0,start));
            str.append(StringLib.abbreviate(line, fm, this.m_maxTextWidth));
            w = this.m_maxTextWidth;
        } else if ( str != null ) {
            str.append(line);
        }
        // update maximum width
        this.m_textDim.width = Math.max(this.m_textDim.width, w);
        
        // compute the text height
        this.m_textDim.height = fm.getHeight() * nlines;
        
        return str==null ? text : str.toString();
    }
    
    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    protected Shape getRawShape(VisualItem item) {
        this.m_text = getText(item);
        double size = item.getSize();
        
        
        // get the value of weight
        double weight = (Double) item.get(SwitchableDisplay.WEIGHT_FIELD);
        // visualization principle: area represents width² in viewer's mind
        double width = 5 + (_baseSize * Math.sqrt(weight));
        
        // get ellipse dimensions
        double iw=width, ih=width;
        
        // get text dimensions
        int tw=0, th=0;
        if ( this.m_text != null ) {
            this.m_text = computeTextDimensions(item, this.m_text, size);
            th = this.m_textDim.height;
            tw = this.m_textDim.width;   
        }
        
        // get bounding box dimensions
        double w=0, h=0;
        switch ( this.m_imagePos ) {
        case Constants.LEFT:
        case Constants.RIGHT:
            w = tw + size*(iw +2*this.m_horizBorder
                   + (tw>0 && iw>0 ? this.m_imageMargin : 0));
            h = Math.max(th, size*ih) + size*2*this.m_vertBorder;
            break;
        case Constants.TOP:
        case Constants.BOTTOM:
            w = Math.max(tw, size*iw) + size*2*this.m_horizBorder;
            h = th + size*(ih + 2*this.m_vertBorder
                   + (th>0 && ih>0 ? this.m_imageMargin : 0));
            break;
        default:
            throw new IllegalStateException(
                "Unrecognized image alignment setting.");
        }
        
        // get the top-left point, using the current alignment settings
        getAlignedPoint(this.m_pt, item, w, h, this.m_xAlign, this.m_yAlign);
        
        if ( this.m_bbox instanceof RoundRectangle2D ) {
            RoundRectangle2D rr = (RoundRectangle2D)this.m_bbox;
            rr.setRoundRect(this.m_pt.getX(), this.m_pt.getY(), w, h,
                            size*this.m_arcWidth, size*this.m_arcHeight);
        } else {
            this.m_bbox.setFrame(this.m_pt.getX(), this.m_pt.getY(), w, h);
        }
        return this.m_bbox;
    }
    
    /**
     * Helper method, which calculates the top-left co-ordinate of an item
     * given the item's alignment.
     */
    protected static void getAlignedPoint(Point2D p, VisualItem item, 
            double w, double h, int xAlign, int yAlign)
    {
        double x = item.getX(), y = item.getY();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0; // safety check
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0; // safety check
        
        if ( xAlign == Constants.CENTER ) {
            x = x-(w/2);
        } else if ( xAlign == Constants.RIGHT ) {
            x = x-w;
        }
        if ( yAlign == Constants.CENTER ) {
            y = y-(h/2);
        } else if ( yAlign == Constants.BOTTOM ) {
            y = y-h;
        }
        p.setLocation(x,y);
    }
    
    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    public void render(Graphics2D g, VisualItem item) {
        RectangularShape shape = (RectangularShape)getShape(item);
        if ( shape == null ) return;
        
        // fill the ellipse, if requested
        int type = getRenderType(item);

        // now render the text and ellipse
        String text = this.m_text;
        Shape ellipse = getEllipse(item);
        
        if ( text == null && ellipse == null )
            return;
                        
        double size = item.getSize();
        boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(),
                                        g.getTransform().getScaleY());
        double x = shape.getMinX() + size*this.m_horizBorder;
        double y = shape.getMinY() + size*this.m_vertBorder;
        
        // render image
        if ( ellipse != null ) {            
            double w = size * ellipse.getBounds().getWidth();
            double h = size * ellipse.getBounds().getHeight();
           
            // determine one co-ordinate based on the image position
            switch ( this.m_imagePos ) {
            case Constants.LEFT:
                x += w + size*this.m_imageMargin;
                break;
            case Constants.RIGHT:
                break;
            case Constants.TOP:
                y += h + size*this.m_imageMargin;
                break;
            case Constants.BOTTOM:
                break;
            default:
//                throw new IllegalStateException(
//                        "Unrecognized image alignment setting.");
            }
            
            // fill ellipse
            if (type==RENDER_TYPE_FILL || type==RENDER_TYPE_DRAW_AND_FILL) {
            	GraphicsLib.paint(g, item, ellipse, getStroke(item), RENDER_TYPE_FILL);
            }
            // draw ellipse border
            if (type==RENDER_TYPE_DRAW || type==RENDER_TYPE_DRAW_AND_FILL) {
                GraphicsLib.paint(g, item, ellipse, getStroke(item), RENDER_TYPE_DRAW);
            }
        }
        
        // render text
        int textColor = item.getTextColor();
        if ( text != null && ColorLib.alpha(textColor) > 0 ) {
            g.setPaint(ColorLib.getColor(textColor));
            g.setFont(this.m_font);
            FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(this.m_font);

            // compute available width
            double tw;
            switch ( this.m_imagePos ) {
            case Constants.TOP:
            case Constants.BOTTOM:
                tw = shape.getWidth() - 2*size*this.m_horizBorder;
                break;
            default:
                tw = this.m_textDim.width;
            }
            
            // compute available height
            double th;
            switch ( this.m_imagePos ) {
            case Constants.LEFT:
            case Constants.RIGHT:
                th = shape.getHeight() - 2*size*this.m_vertBorder;
                break;
            default:
                th = this.m_textDim.height;
            }
            
            // compute starting y-coordinate
            y += fm.getAscent();
            switch ( this.m_vTextAlign ) {
            case Constants.TOP:
                break;
            case Constants.BOTTOM:
                y += th - this.m_textDim.height;
                break;
            case Constants.CENTER:
                y += (th - this.m_textDim.height)/2;
            }
            
            // render each line of text
            int lh = fm.getHeight(); // the line height
            int start = 0, end = text.indexOf(this.m_delim);
            for ( ; end >= 0; y += lh ) {
                drawString(g, fm, text.substring(start, end), useInt, x, y, tw);
                start = end+1;
                end = text.indexOf(this.m_delim, start);   
            }
            drawString(g, fm, text.substring(start), useInt, x, y, tw);
        }
    }
    
    /**
	 * @param item
	 * @return
	 */
	private Shape getEllipse(VisualItem item) {
		Ellipse2D ellipse = new Ellipse2D.Double();
		
        double x = item.getX();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        double y = item.getY();
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;
        
        // get the value of weight
        double weight = (Double) item.get(SwitchableDisplay.WEIGHT_FIELD);
        // visualization principle: area represents width² in viewer's mind
        double width = 5 + (_baseSize * Math.sqrt(weight));
        
        // Center the shape around the specified x and y
        if ( width > 1 ) {
            x = x - width/2;
            y = y - width/2 + 5;
        }
        
        ellipse.setFrame(x, y, width, width);
		return ellipse;
	}

	private final void drawString(Graphics2D g, FontMetrics fm, String text,
            boolean useInt, double x, double y, double w)
    {
        // compute the x-coordinate
        double tx;
        switch ( this.m_hTextAlign ) {
        case Constants.LEFT:
            tx = x;
            break;
        case Constants.RIGHT:
            tx = x + w - fm.stringWidth(text);
            break;
        case Constants.CENTER:
            tx = x + (w - fm.stringWidth(text)) / 2;
            break;
        default:
            throw new IllegalStateException(
                    "Unrecognized text alignment setting.");
        }
        // use integer precision unless zoomed-in
        // results in more stable drawing
        if ( useInt ) {
            g.drawString(text, (int)tx, (int)y);
        } else {
            g.drawString(text, (float)tx, (float)y);
        }
    }

	/**
     * Draws the specified shape into the provided Graphics context, using
     * stroke and fill color values from the specified VisualItem. This method
     * can be called by subclasses in custom rendering routines. 
     */
    protected void drawShape(Graphics2D g, VisualItem item, Shape shape) {
        GraphicsLib.paint(g, item, shape, getStroke(item), getRenderType(item));
    }
    
    /**
     * Returns the image factory used by this renderer.
     * @return the image factory
     */
    public ImageFactory getImageFactory() {
        if ( this.m_images == null ) this.m_images = new ImageFactory();
        return this.m_images;
    }
    
    /**
     * Sets the image factory used by this renderer.
     * @param ifact the image factory
     */
    public void setImageFactory(ImageFactory ifact) {
        this.m_images = ifact;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Get the horizontal text alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @return the horizontal text alignment
     */
    public int getHorizontalTextAlignment() {
        return this.m_hTextAlign;
    }
    
    /**
     * Set the horizontal text alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @param halign the desired horizontal text alignment
     */
    public void setHorizontalTextAlignment(int halign) {
        if ( halign != Constants.LEFT &&
             halign != Constants.RIGHT &&
             halign != Constants.CENTER )
           throw new IllegalArgumentException(
                   "Illegal horizontal text alignment value.");
        this.m_hTextAlign = halign;
    }
    
    /**
     * Get the vertical text alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @return the vertical text alignment
     */
    public int getVerticalTextAlignment() {
        return this.m_vTextAlign;
    }
    
    /**
     * Set the vertical text alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     * @param valign the desired vertical text alignment
     */
    public void setVerticalTextAlignment(int valign) {
        if ( valign != Constants.TOP &&
             valign != Constants.BOTTOM &&
             valign != Constants.CENTER )
            throw new IllegalArgumentException(
                    "Illegal vertical text alignment value.");
        this.m_vTextAlign = valign;
    }
    
    /**
     * Get the horizontal image alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     * @return the horizontal image alignment
     */
    public int getHorizontalImageAlignment() {
        return this.m_hImageAlign;
    }
    
    /**
     * Set the horizontal image alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     * @param halign the desired horizontal image alignment
     */
    public void setHorizontalImageAlignment(int halign) {
        if ( halign != Constants.LEFT &&
             halign != Constants.RIGHT &&
             halign != Constants.CENTER )
           throw new IllegalArgumentException(
                   "Illegal horizontal text alignment value.");
        this.m_hImageAlign = halign;
    }
    
    /**
     * Get the vertical image alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     * @return the vertical image alignment
     */
    public int getVerticalImageAlignment() {
        return this.m_vImageAlign;
    }
    
    /**
     * Set the vertical image alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     * @param valign the desired vertical image alignment
     */
    public void setVerticalImageAlignment(int valign) {
        if ( valign != Constants.TOP &&
             valign != Constants.BOTTOM &&
             valign != Constants.CENTER )
            throw new IllegalArgumentException(
                    "Illegal vertical text alignment value.");
        this.m_vImageAlign = valign;
    }
    
    /**
     * Get the image position, determining where the image is placed with
     * respect to the text. One of {@link Constants#LEFT},
     * {@link Constants#RIGHT}, {@link Constants#TOP}, or
     * {@link Constants#BOTTOM}.  The default is left.
     * @return the image position
     */
    public int getImagePosition() {
        return this.m_imagePos;
    }
    
    /**
     * Set the image position, determining where the image is placed with
     * respect to the text. One of {@link Constants#LEFT},
     * {@link Constants#RIGHT}, {@link Constants#TOP}, or
     * {@link Constants#BOTTOM}.  The default is left.
     * @param pos the desired image position
     */
    public void setImagePosition(int pos) {
        if ( pos != Constants.TOP &&
             pos != Constants.BOTTOM &&
             pos != Constants.LEFT &&
             pos != Constants.RIGHT &&
             pos != Constants.CENTER )
           throw new IllegalArgumentException(
                   "Illegal image position value.");
        this.m_imagePos = pos;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Get the horizontal alignment of this node with respect to its
     * x, y coordinates.
     * @return the horizontal alignment, one of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment() {
        return this.m_xAlign;
    }
    
    /**
     * Get the vertical alignment of this node with respect to its
     * x, y coordinates.
     * @return the vertical alignment, one of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment() {
        return this.m_yAlign;
    }
    
    /**
     * Set the horizontal alignment of this node with respect to its
     * x, y coordinates.
     * @param align the horizontal alignment, one of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */ 
    public void setHorizontalAlignment(int align) {
        this.m_xAlign = align;
    }
    
    /**
     * Set the vertical alignment of this node with respect to its
     * x, y coordinates.
     * @param align the vertical alignment, one of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */ 
    public void setVerticalAlignment(int align) {
        this.m_yAlign = align;
    }
    
    /**
     * Returns the amount of padding in pixels between the content 
     * and the border of this item along the horizontal dimension.
     * @return the horizontal padding
     */
    public int getHorizontalPadding() {
        return this.m_horizBorder;
    }
    
    /**
     * Sets the amount of padding in pixels between the content 
     * and the border of this item along the horizontal dimension.
     * @param xpad the horizontal padding to set
     */
    public void setHorizontalPadding(int xpad) {
        this.m_horizBorder = xpad;
    }
    
    /**
     * Returns the amount of padding in pixels between the content 
     * and the border of this item along the vertical dimension.
     * @return the vertical padding
     */
    public int getVerticalPadding() {
        return this.m_vertBorder;
    }
    
    /**
     * Sets the amount of padding in pixels between the content 
     * and the border of this item along the vertical dimension.
     * @param ypad the vertical padding
     */
    public void setVerticalPadding(int ypad) {
        this.m_vertBorder = ypad;
    }
    
    /**
     * Get the padding, in pixels, between an image and text.
     * @return the padding between an image and text
     */
    public int getImageTextPadding() {
        return this.m_imageMargin;
    }
    
    /**
     * Set the padding, in pixels, between an image and text.
     * @param pad the padding to use between an image and text
     */
    public void setImageTextPadding(int pad) {
        this.m_imageMargin = pad;
    }
    
}
