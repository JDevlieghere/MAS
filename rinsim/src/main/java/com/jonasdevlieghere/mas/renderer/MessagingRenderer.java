package com.jonasdevlieghere.mas.renderer;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.ui.renderers.*;

import javax.annotation.Nullable;
import java.util.Set;

public class MessagingRenderer implements ModelRenderer {

    protected final static RGB GREEN = new RGB(0, 255, 0);
    protected final static RGB RED = new RGB(255, 0, 0);

    protected RoadModel rm;

    @Override
    public void renderStatic(GC gc, ViewPort vp) {

    }

    @Override
    public void renderDynamic(GC gc, ViewPort vp, long time) {
        final int size = 4;

        final Set<DeliveryTruck> objects = rm
                .getObjectsOfType(DeliveryTruck.class);

        synchronized (objects) {
            for (final DeliveryTruck a : objects) {
                Point p = a.getPosition();
                if (p == null) {
                    continue;
                }
                final int x = (int) (vp.origin.x + (p.x - vp.rect.min.x) * vp.scale);
                final int y = (int) (vp.origin.y + (p.y - vp.rect.min.y) * vp.scale);

                final int radius = (int) (a.getRadius() * vp.scale);

                Color c = new Color(gc.getDevice(), GREEN);

                gc.setForeground(c);
                gc.setBackground(c);

                gc.setAlpha(50);
                gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
                gc.setAlpha(255);

                gc.fillOval(x - size, y - size, size * 2, size * 2);

                gc.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                gc.drawText("r:" + a.getNoReceived(), x, y, true);

                final Set<DeliveryTruck> communicatedWith = a.getCommunicatedWith();
                for (final DeliveryTruck cw : communicatedWith) {
                    p = cw.getPosition();
                    if (p == null) {
                        continue;
                    }
                    final int xCW = (int) (vp.origin.x + (p.x - vp.rect.min.x) * vp.scale);
                    final int yCW = (int) (vp.origin.y + (p.y - vp.rect.min.y) * vp.scale);
                    gc.drawLine(x, y, xCW, yCW);
                }
            }
        }
    }

    @Nullable
    @Override
    public ViewRect getViewRect() {
        return null;
    }

    @Override
    public void registerModelProvider(ModelProvider mp) {
        this.rm = mp.getModel(RoadModel.class);
    }
}
