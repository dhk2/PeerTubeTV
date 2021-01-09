package net.anticlimacticteleservices.peertube.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LeanBackHeaderCategory implements Serializable {


    String thumbnailURL;
    String name;
    boolean isLoading;
    ArrayList<Video> videos;



    public LeanBackHeaderCategory(String name) {
        this.name = name;
        this.isLoading =false;
        this.thumbnailURL ="";
        this.videos=new ArrayList<Video>();
    }
    public LeanBackHeaderCategory() {
        this.name = "";
        this.isLoading =false;
        this.thumbnailURL ="";
        this.videos=new ArrayList<Video>();
    }
    public LeanBackHeaderCategory(String name, ArrayList<Video> allVideos){
        this.name=name;
        this.thumbnailURL ="";
        this.isLoading =false;
        this.videos=allVideos;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }

    public ArrayList<Video> getVideos() { return videos; }

    public void setVideos(ArrayList<Video> videos) { this.videos = videos; }

    public void addVideo(Video toAdd){
        for (Video test:this.videos){
            if (test.getUuid().equals(toAdd.getUuid())){
                return;
            }
        }
        this.videos.add(toAdd);
    }
    public void addAllVideo(ArrayList<Video> toAdd){
        if (this.videos == null){
            this.videos = toAdd;
        } else {
            this.videos.addAll(toAdd);
        }
    }
}
