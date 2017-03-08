package com.app.teacup.bean.movie;


import java.util.List;

public class MovieDetailInfo {

    private String movieBlockName;
    private String moreUrl;
    private List<MovieItemInfo> movieInfoList;

    public String getMoreUrl() {
        return moreUrl;
    }

    public void setMoreUrl(String moreUrl) {
        this.moreUrl = moreUrl;
    }

    public String getMovieBlockName() {
        return movieBlockName;
    }

    public void setMovieBlockName(String movieBlockName) {
        this.movieBlockName = movieBlockName;
    }

    public List<MovieItemInfo> getMovieInfoList() {
        return movieInfoList;
    }

    public void setMovieInfoList(List<MovieItemInfo> movieInfoList) {
        this.movieInfoList = movieInfoList;
    }
}
