package com.library.repository

import com.library.Document
import com.library.KakaoBookResponse
import com.library.Meta
import com.library.feign.KakaoClient
import spock.lang.Specification

import java.time.LocalDate

class KakaoBookRepositoryTest extends Specification {
    BookRepository bookRepository

    KakaoClient kakaoClient = Mock()

    void setup() {
        bookRepository = new KakaoBookRepository(kakaoClient)
    }

    def "search 호출시 적절한 데이터로 변환한다."() {
        given:
        def documents = [
                new Document("제목1", ["저자1"], "1165219468", "길벗1", "2022-04-25T00:00:00.000+09:00"),
                new Document("제목2", ["저자2"], "1165219468", "길벗2", "2022-04-25T00:00:00.000+09:00")
        ]
        def meta = new Meta(false, 1, 10)

        def response = new KakaoBookResponse(documents, meta)

        and:
        1 * kakaoClient.search("HTTP", 1, 2) >> response

        when:
        def result = bookRepository.search("HTTP", 1, 2)

        then:
        verifyAll(result) {
            size() == 2
            page() == 1
            totalElements() == 10
            contents().size() == 2
            contents().get(0).pubDate() == LocalDate.of(2022, 4, 25)
        }

    }
}
