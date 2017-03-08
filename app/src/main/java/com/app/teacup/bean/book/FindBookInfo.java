package com.app.teacup.bean.book;


import java.io.Serializable;

public class FindBookInfo implements Serializable {
    private String mImgUrl;
    private String mBookTitle;
    private String mBookContent;
    private String mSummary;
    private String mAuthor;
    private String mTable;

    public String getmSummary() {
        return mSummary;
    }

    public void setmSummary(String mSummary) {
        this.mSummary = mSummary;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getmTable() {
        return mTable;
    }

    public void setmTable(String mTable) {
        this.mTable = mTable;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmBookTitle() {
        return mBookTitle;
    }

    public void setmBookTitle(String mBookTitle) {
        this.mBookTitle = mBookTitle;
    }

    public String getmBookContent() {
        return mBookContent;
    }

    public void setmBookContent(String mBookContent) {
        this.mBookContent = mBookContent;
    }

    @Override
    public String toString() {
        return "FindBookInfo{" +
                "mImgUrl='" + mImgUrl + '\'' +
                ", mBookTitle='" + mBookTitle + '\'' +
                ", mBookContent='" + mBookContent + '\'' +
                '}';
    }
}
