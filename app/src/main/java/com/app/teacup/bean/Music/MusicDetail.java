package com.app.teacup.bean.Music;


import java.io.Serializable;

public class MusicDetail implements Serializable {
    private String musicName;
    private String musicPlayer;
    private String imgUrl;
    private String musicUrl;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicPlayer() {
        return musicPlayer;
    }

    public void setMusicPlayer(String musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }
}
