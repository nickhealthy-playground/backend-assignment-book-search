package com.library.repository;

import com.library.controller.response.PageResult;
import com.library.controller.response.SearchResponse;
import org.springframework.stereotype.Repository;

@Repository
public class KakaoBookRepository implements BookRepository{
    @Override
    public PageResult<SearchResponse> search(String query, int page, int size) {
        return null;
    }
}
