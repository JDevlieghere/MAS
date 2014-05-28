package com.jonasdevlieghere.mas.simulation;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.ui.renderers.ModelRenderer;
import rinde.sim.ui.renderers.ViewPort;
import rinde.sim.ui.renderers.ViewRect;

import javax.annotation.Nullable;

public class BeaconRenderer implements ModelRenderer {

  protected final static RGB GREEN = new RGB(0, 255, 0);
  protected final static RGB RED = new RGB(255, 0, 0);

  protected BeaconModel beaconModel;

  @Override
  public void renderStatic(GC gc, ViewPort vp) {}

  @Override
  public void renderDynamic(GC gc, ViewPort vp, long time) {}

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
