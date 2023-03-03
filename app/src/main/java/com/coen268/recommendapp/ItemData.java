package com.coen268.recommendapp;


public class ItemData {
    public static final int TYPE_ORI = 0;
    public static final int TYPE_PIC = 1;
    public static final int TYPE_PRODUCT = 2;

    public ItemData(int sizeType, int jumpType, String url) {
        this.sizeType = sizeType;
        this.jumpType = jumpType;
        this.url = url;
    }
    public ItemData(int sizeType, int jumpType, String url, String webUrl) {
        this.sizeType = sizeType;
        this.jumpType = jumpType;
        this.url = url;
        this.webUrl = webUrl;
    }

    public ItemData(int sizeType, int jumpType, String url, Float score) {
        this.sizeType = sizeType;
        this.jumpType = jumpType;
        this.url = url;
        this.score = score;
    }

    public ItemData(int sizeType, int jumpType, String url, String webUrl, String price, String desc) {
        this.sizeType = sizeType;
        this.jumpType = jumpType;
        this.url = url;
        this.webUrl = webUrl;
        this.price = price;
        this.desc = desc;
    }

    int sizeType;
    int jumpType;
    String url;

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    String webUrl;
    String price;

    public void setDesc(String desc) {
        this.desc = desc;
    }

    String desc;

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    Float score;

    public String getUrl() {
        return url;
    }

    public int getSizeType() {
        return sizeType;
    }

    public int getJumpType() {
        return jumpType;
    }
}
