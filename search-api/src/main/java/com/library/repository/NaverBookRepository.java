package com.library.repository;

import com.library.controller.response.PageResult;
import com.library.controller.response.SearchResponse;
import com.library.util.DateUtils;
import com.library.Item;
import com.library.NaverBookResponse;
import com.library.feign.NaverClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class NaverBookRepository implements BookRepository {
    private final NaverClient naverClient;

    @Override
    public PageResult<SearchResponse> search(String query, int page, int size) {
        NaverBookResponse response = naverClient.search(query, toStart(page, size), size);
        List<SearchResponse> responses = response.getItems().stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page, size, response.getTotal(), responses);
    }

    // 네이버 start는 페이지 번호가 아니라 1부터 시작하는 첫 항목의 위치다 (카카오 page와 의미가 다름)
    private int toStart(int page, int size) {
        return (page - 1) * size + 1;
    }

    private SearchResponse createResponse(Item item) {
        return SearchResponse.builder()
                .title(item.getTitle())
                .author(item.getAuthor())
                .publisher(item.getPublisher())
                .pubDate(DateUtils.parseYYYYMMDD(item.getPubDate()))
                .isbn(item.getIsbn())
                .build();
    }
}
