package com.jonasdevlieghere.mas.activity;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/29/14
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Activity {

    private ActivityStatus status;
    private ActivityUser user;

    public Activity(ActivityUser user){
        this.user = user;
        setStatus(ActivityStatus.NORMAL);
    }

    public abstract void execute();


    public void setStatus(ActivityStatus status){
        this.status = status;
    }

    public ActivityStatus getStatus(){
        return this.status;
    }

    public ActivityUser getUser() {
        return user;
    }

}
