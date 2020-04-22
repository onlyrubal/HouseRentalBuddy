package com.example.finalproject;

public class RentalPost {
    public String user_id, image_url, desc, price, image_thumb;

    public RentalPost() { }

    public RentalPost(String user_id, String image_url, String desc, String price, String image_thumb) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.price = price;
        this.image_thumb = image_thumb;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

}
