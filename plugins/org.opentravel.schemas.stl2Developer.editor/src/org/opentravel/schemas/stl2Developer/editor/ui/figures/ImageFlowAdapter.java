
package org.opentravel.schemas.stl2Developer.editor.ui.figures;

import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.BidiInfo;
import org.eclipse.draw2d.text.BidiProcessor;
import org.eclipse.draw2d.text.ContentBox;
import org.eclipse.draw2d.text.FlowContext;
import org.eclipse.draw2d.text.FlowFigure;
import org.eclipse.draw2d.text.FlowFigureLayout;

public class ImageFlowAdapter extends FlowFigure {

    public static final int FONT_ASCENT = 3;
    private FlowContext context;
    private FigureBox box = new FigureBox();

    @Override
    protected void contributeBidi(BidiProcessor proc) {
        box.setBidiLevel(-1);
        // contributes a single object replacement char
        proc.add(this, '\ufffc');
    }

    @Override
    protected FlowFigureLayout createDefaultFlowLayout() {
        return null;
    }

    @Override
    protected void layout() {
        int wHint = context.getRemainingLineWidth();
        if (wHint == Integer.MAX_VALUE)
            wHint = -1;
        Dimension prefSize = getPreferredSize(wHint, -1);
        if (context.isCurrentLineOccupied() && prefSize.width > context.getRemainingLineWidth()) {
            context.endLine();
            prefSize = getPreferredSize(context.getRemainingLineWidth(), -1);
        }
        box.setSize(prefSize);
        context.addToCurrentLine(box);
    }

    @Override
    public void postValidate() {
        setBounds(new Rectangle(box.getX(), box.getBaseline() - box.ascent, box.getWidth(),
                box.ascent));
        super.layout();
        for (Iterator<?> itr = getChildren().iterator(); itr.hasNext();)
            ((IFigure) itr.next()).validate();
    }

    @Override
    public void setBidiInfo(BidiInfo info) {
        box.setBidiLevel(info.levelInfo[0]);
    }

    @Override
    public void setBounds(Rectangle rect) {
        int x = bounds.x, y = bounds.y;

        boolean resize = (rect.width != bounds.width) || (rect.height != bounds.height), translate = (rect.x != x)
                || (rect.y != y);

        if ((resize || translate) && isVisible())
            erase();
        if (translate) {
            int dx = rect.x - x;
            int dy = rect.y - y;
            primTranslate(dx, dy);
        }

        bounds.width = rect.width;
        bounds.height = rect.height;

        if (translate || resize) {
            fireFigureMoved();
            repaint();
        }
    }

    @Override
    public void setFlowContext(FlowContext flowContext) {
        context = flowContext;
    }

    @Override
    public void validate() {
        if (isValid())
            return;
        setValid(true);
        layout();
    }

    private class FigureBox extends ContentBox {
        private int ascent;

        @Override
        public boolean containsPoint(int x, int y) {
            return ImageFlowAdapter.this.containsPoint(x, y);
        }

        @Override
        public int getAscent() {
            return ascent - FONT_ASCENT;
        }

        @Override
        public int getDescent() {
            return 0;
        }

        public void setSize(Dimension size) {
            ascent = size.height;
            setWidth(size.width);
        }

        @Override
        public int getBaseline() {
            return super.getBaseline() + FONT_ASCENT;
        }
    }

}