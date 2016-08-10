package com.app.bean.Music;

import java.util.List;

public class MusicDetailInfo {
    private String type;
    private String content;
    private List<MusicDetail> musicList;

    public List<MusicDetail> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<MusicDetail> musicList) {
        this.musicList = musicList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
