package com.example.xyzreader.api;

import com.example.xyzreader.data.model.Article;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Created by Amrendra Kumar on 08/03/16.
 */
public interface ArticlesEndPointsInterface {

    @GET("u/231329/xyzreader_data/data.json")
    Call<List<Article>> getArticles();
}
