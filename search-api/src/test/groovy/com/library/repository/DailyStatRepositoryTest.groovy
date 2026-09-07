package com.library.repository

import com.library.entity.DailyStat
import jakarta.persistence.EntityManager
import libarary.feign.NaverClient
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.LocalDateTime

@ActiveProfiles("test")
@DataJpaTest
class DailyStatRepositoryTest extends Specification {
    @Autowired
    DailyStatRepository dailyStatRepository

    @Autowired
    EntityManager entityManager

    @SpringBean
    NaverClient naverClient = Mock()

    def "저장 후 조회가 된다."() {
        given:
        def givenQuery = "HTTP"

        when:
        def dailyStat = new DailyStat(givenQuery, LocalDateTime.now())
        def saved = dailyStatRepository.saveAndFlush(dailyStat)

        then: "실제 저장이 된다."
        saved.id != null

        when: "entityManager를 clear하고 재조회한다."
        entityManager.clear()
        def result = dailyStatRepository.findById(saved.id)

        then:
        verifyAll {
            result.isPresent()
            result.get().query == givenQuery
        }

        entityManager.close()
    }

    def "쿼리의 카운트를 조회한다."() {
        given:
        def givenQuery = "HTTP"
        def now = LocalDateTime.of(2026, 9, 7, 0,0,0)

        def stat1 = new DailyStat(givenQuery, now.plusMinutes(10))
        def stat2 = new DailyStat(givenQuery, now.minusMinutes(1))
        def stat3 = new DailyStat(givenQuery, now.plusMinutes(10))
        def stat4 = new DailyStat("JAVA", now.plusMinutes(10))

        dailyStatRepository.saveAll([stat1, stat2, stat3, stat4])

        when:
        def result = dailyStatRepository.countByQueryAndEventDateTimeBetween(givenQuery, now, now.plusDays(1))

        then:
        result == 2
    }
}
