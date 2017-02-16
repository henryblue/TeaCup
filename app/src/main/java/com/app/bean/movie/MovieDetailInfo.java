package com.app.bean.movie;


import java.util.List;

public class MovieDetailInfo {

    String movieBlockName;
    List<MovieItemInfo> movieInfoList;

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
