package com.example.xyzreader.api;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Amrendra Kumar on 08/03/16.
 */
public class ArticlesApiClientService {
    public static final String API_BASE_URL = "https://dl.dropboxusercontent.com/";

    private static ArticlesEndPointsInterface articleEndPointInterface = null;

    private ArticlesApiClientService() {
    }

    public static ArticlesEndPointsInterface getInstance() {
        if (articleEndPointInterface == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            articleEndPointInterface = retrofit.create(ArticlesEndPointsInterface.class);
        }
        return articleEndPointInterface;
    }
}
