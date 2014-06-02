package com.jonasdevlieghere.mas.simulation;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconStatus;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
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

public class BeaconRenderer implements ModelRenderer {

    protected final static RGB BLACK = new RGB(0, 0, 0);
    protected final static RGB BORDEAUX = new RGB(95, 2, 31);
    protected final static RGB GREEN = new RGB(0, 255, 0);
    protected final static RGB YELLOW = new RGB(225, 255, 0);

    protected BeaconModel beaconModel;

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
                final int r = (int) (parcel.getRadius() * vp.scale);

                if(parcel.getStatus() != BeaconStatus.INACTIVE) {
                    RGB rgb = GREEN;
                    if(parcel.getStatus() == BeaconStatus.BUSY){
                        rgb = YELLOW;
                    }
                    gc.setBackground(new Color(gc.getDevice(), rgb));
                    gc.setForeground(new Color(gc.getDevice(), rgb));
                    gc.setAlpha(10);
                    gc.fillOval(x - r, y - r, 2 * r, 2 * r);
                    gc.setAlpha(50);
                    gc.drawOval(x - r, y - r, r * 2, r * 2);
                    gc.setAlpha(255);
                }
            }
        }

        final List<DeliveryTruck> trucks = beaconModel.getAllTruckBeacons();
        synchronized (trucks) {
            for(DeliveryTruck truck : trucks){
                Point position = truck.getPosition();

                final int x = (int) (vp.origin.x + (position.x - vp.rect.min.x) * vp.scale);
                final int y = (int) (vp.origin.y + (position.y - vp.rect.min.y) * vp.scale);
                final int r = (int) (truck.getRadius() * vp.scale);
                RGB rgb = BLACK;
                if(truck.getStatus() == BeaconStatus.BUSY){
                    rgb = BORDEAUX;
                }
                gc.setBackground(new Color(gc.getDevice(), rgb));
                gc.setForeground(new Color(gc.getDevice(), rgb));
                gc.setAlpha(10);
                gc.fillOval(x - r, y - r, 2 * r, 2 * r);
                gc.setAlpha(50);
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
