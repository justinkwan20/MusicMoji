package com.example.musicmoji;

public class ServerSongsData {

    public String title;
    public String artist;

    public ServerSongsData(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public ServerSongsData(){}

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
