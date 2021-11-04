package com.example.whetherapp.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WhetherModel {

    @SerializedName("timezone")
    @Expose
    private Integer timezone;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("cod")
    @Expose
    private Integer cod;
    @SerializedName("weather")
    @Expose
    public List<Weather> weather = null;
    @SerializedName("main")
    @Expose
    public Main main;

    public class Weather{
        @SerializedName("main")
        @Expose
        public String main;
    }

    public class Main{
        @SerializedName("temp")
        @Expose
        public String temp;
        @SerializedName("feels_like")
        @Expose
        public String feelsLike;
        @SerializedName("temp_min")
        @Expose
        public String tempMin;
        @SerializedName("temp_max")
        @Expose
        public String tempMax;
        @SerializedName("pressure")
        @Expose
        public String pressure;
        @SerializedName("humidity")
        @Expose
        public String humidity;

    }
}


