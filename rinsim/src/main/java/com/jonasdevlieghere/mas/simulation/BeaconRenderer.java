package com.jonasdevlieghere.mas.simulation;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.ui.renderers.ModelRenderer;
import rinde.sim.ui.renderers.ViewPort;
import rinde.sim.ui.renderers.ViewRect;

import javax.annotation.Nullable;
import java.util.List;

class BeaconRenderer implements ModelRenderer {

    private final static int FILL_ALPHA = 10;
    private final static int DRAW_ALPHA = 50;

    private final static RGB BLACK = new RGB(0, 0, 0);
    private final static RGB RED = new RGB(255, 0, 0);
    private final static RGB GREEN = new RGB(0, 255, 0);
    private final static RGB BLUE = new RGB(0, 0, 255);
    protected final static RGB YELLOW = new RGB(255, 255, 0);

    private BeaconModel beaconModel;

    @Override
    public void renderStatic(GC gc, ViewPort vp) {}

    @Override
    public void renderDynamic(GC gc, ViewPort vp, long time) {
        final List<BeaconParcel> parcels = beaconModel.getAllParcelBeacons();
        synchronized (parcels) {
            for(BeaconParcel parcel : parcels){

                Point position = parcel.getPosition();

                final int x = (int) (vp.origin.x + (position.x - vp.rect.min.x) * vp.scale);
                final int y = (int) (vp.origin.y + (position.y - vp.rect.min.y) * vp.scale);
                final int r = (int) (parcel.getBeaconRadius() * vp.scale);

                if(parcel.getBeaconStatus() != BeaconStatus.INACTIVE) {
                    RGB rgb = GREEN;
                    if(parcel.getBeaconStatus() == BeaconStatus.SLAVE){
                        rgb = RED;
                    }
                    gc.setBackground(new Color(gc.getDevice(), rgb));
                    gc.setForeground(new Color(gc.getDevice(), rgb));
                    gc.setAlpha(FILL_ALPHA);
                    gc.fillOval(x - r, y - r, 2 * r, 2 * r);
                    gc.setAlpha(DRAW_ALPHA);
                    gc.drawOval(x - r, y - r, r * 2, r * 2);
                    gc.setAlpha(255);
                }
            }
        }

        final List<BeaconTruck> trucks = beaconModel.getAllTruckBeacons();
        synchronized (trucks) {
            for(BeaconTruck truck : trucks){
                Point position = truck.getPosition();

                final int x = (int) (vp.origin.x + (position.x - vp.rect.min.x) * vp.scale);
                final int y = (int) (vp.origin.y + (position.y - vp.rect.min.y) * vp.scale);
                final int r = (int) (truck.getBeaconRadius() * vp.scale);
                RGB rgb = BLACK;
                if(truck.getBeaconStatus() == BeaconStatus.MASTER || truck.getBeaconStatus() == BeaconStatus.SLAVE){
                    rgb = BLUE;
                }
                gc.setBackground(new Color(gc.getDevice(), rgb));
                gc.setForeground(new Color(gc.getDevice(), rgb));
                gc.setAlpha(FILL_ALPHA);
                gc.fillOval(x - r, y - r, 2 * r, 2 * r);
                gc.setAlpha(DRAW_ALPHA);
                gc.drawOval(x - r, y - r, r * 2, r * 2);
                gc.setAlpha(255);
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
        beaconModel = mp.getModel(BeaconModel.class);
    }

}
