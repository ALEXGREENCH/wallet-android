package com.mycelium.wallet.external.news.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class News implements Serializable {
    @JsonProperty("ID")
    public int id;

    @JsonProperty("title")
    public String title;

    @JsonProperty("content")
    public String content;

    @JsonProperty("date")
    public Date date;

    @JsonProperty("author")
    public Author author;

    @JsonProperty("short_URL")
    public String link;

    @JsonProperty("categories")
    public Map<String, Category> categories;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News news = (News) o;
        return id == news.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
