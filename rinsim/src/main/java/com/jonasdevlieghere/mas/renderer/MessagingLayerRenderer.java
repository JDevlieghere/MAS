package com.jonasdevlieghere.mas.renderer;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.ui.renderers.CanvasRenderer;
import rinde.sim.ui.renderers.UiSchema;
import rinde.sim.ui.renderers.ViewPort;
import rinde.sim.ui.renderers.ViewRect;

import javax.annotation.Nullable;
import java.util.Set;

public class MessagingLayerRenderer implements CanvasRenderer {

  protected RoadModel rm;
  private final UiSchema uiSchema;

  public MessagingLayerRenderer(RoadModel rm, UiSchema uiSchema) {
    this.rm = rm;
    this.uiSchema = uiSchema;
  }

  @Override
  public void renderDynamic(GC gc, ViewPort vp, long time) {
    final int size = 4;
    uiSchema.initialize(gc.getDevice());

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

        Color c = null;
        if (a.getReliability() < 0.15) {
          c = uiSchema.getColor(DeliveryTruck.C_BLACK);
        } else if (a.getReliability() >= 0.15 && a.getReliability() < 0.3) {
          c = uiSchema.getColor(DeliveryTruck.C_YELLOW);
        } else {
          c = uiSchema.getColor(DeliveryTruck.C_GREEN);
        }

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

  @Override
  public void renderStatic(GC gc, ViewPort vp) {}

  @Nullable
  @Override
  public ViewRect getViewRect() {
    return null;
  }

}
