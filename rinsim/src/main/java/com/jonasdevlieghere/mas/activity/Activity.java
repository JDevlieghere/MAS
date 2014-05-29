package com.jonasdevlieghere.mas.activity;

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
