package com.app.bean.Music;


import java.io.Serializable;

public class MusicInfo implements Serializable {
    private String nextUrl;
    private String imgUrl;
    private String title;
    private String happyNum;
    private String infoNum;

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public String getHappyNum() {
        return happyNum;
    }

    public void setHappyNum(String happyNum) {
        this.happyNum = happyNum;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getInfoNum() {
        return infoNum;
    }

    public void setInfoNum(String infoNum) {
        this.infoNum = infoNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
