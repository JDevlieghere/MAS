package com.jonasdevlieghere.mas.communication;

import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.Parcel;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/28/14
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Assignment extends Message{
    private Parcel parcel;

    public Assignment(CommunicationUser sender, Parcel parcel) {
        super(sender);
        setParcel(parcel);
    }

    private void setParcel(Parcel parcel){
        this.parcel = parcel;
    }

    public Parcel getParcel(){
        return parcel;
    }

}
