package com.example.psato.paulosato_sample;

/**
 * Created by psato on 10/4/15.
 */
public class Constants {
    private static final String SHOW_NAME = "game-of-thrones";
    public static final String SEASONS = "1";
    public static final String CLIENT_ID = "3f257df125b8c84de35d5b32e676532ca7a879659ba2f84619087bbb2ca0e563";
    private static final String CLIENT_SECRECT = "cb98ef76b2a11b243e75d62bbdc15545cdcaeb5ec6665bb701a04c359207b257";
    public static final String URL_GET_EPISODE_LIST = "https://api-v2launch.trakt.tv/shows/"+SHOW_NAME+"/seasons/"+SEASONS;
    public static final String URL_GET_SEASON_RATING = "https://api-v2launch.trakt.tv/shows/"+SHOW_NAME+"/seasons/"+SEASONS+"/ratings";
    public static final String URL_GET_SEASON_IMAGES = "https://api-v2launch.trakt.tv/shows/"+SHOW_NAME+"/seasons?extended=images";
    public static final String URL_GET_SHOW_IMAGES = "https://api-v2launch.trakt.tv/shows/"+SHOW_NAME+"?extended=images";
}
