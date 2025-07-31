package Risosu.it.EGDDTheMovieDB25Julio.ML;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Language {

    @JsonProperty("iso_639_1")
    private String iso639_1;
    @JsonProperty("english_name")
    String english_name;
    @JsonProperty("name")
    String name;

    public String getIso639_1() {
        return iso639_1;
    }

    public void setIso639_1(String iso639_1) {
        this.iso639_1 = iso639_1;
    }

    

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String english_name) {
        this.english_name = english_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
