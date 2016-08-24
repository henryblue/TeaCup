package com.app.bean.Read;


import java.util.List;

public class ReadCadInfo {
    private String cadTitle;
    private String cadContent;
    private String more;
    private String moreUrl;
    private List<ReadInfo> readList;

    public String getCadContent() {
        return cadContent;
    }

    public void setCadContent(String cadContent) {
        this.cadContent = cadContent;
    }

    public String getCadTitle() {
        return cadTitle;
    }

    public void setCadTitle(String cadTitle) {
        this.cadTitle = cadTitle;
    }

    public String getMore() {
        return more;
    }

    public void setMore(String more) {
        this.more = more;
    }

    public String getMoreUrl() {
        return moreUrl;
    }

    public void setMoreUrl(String moreUrl) {
        this.moreUrl = moreUrl;
    }

    public List<ReadInfo> getReadList() {
        return readList;
    }

    public void setReadList(List<ReadInfo> readList) {
        this.readList = readList;
    }
}
