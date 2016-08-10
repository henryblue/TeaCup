package com.app.bean.book;


import java.io.Serializable;

public class FindBookInfo implements Serializable {
    String mImgUrl;
    String mBookTitle;
    String mBookContent;
    String mSummary;
    String mAuthor;
    String mTable;

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
