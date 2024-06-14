package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import study.querydsl.entity.Hello;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.QHello;

@SpringBootTest
@Transactional
//@Commit
class QuerydslTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    void querydslHello() {
        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory query = new JPAQueryFactory(em);
        QHello qHello = new QHello("h");

        Hello result = query.selectFrom(qHello).fetchOne();

        Assertions.assertThat(result).isEqualTo(hello);
        Assertions.assertThat(result.getId()).isEqualTo(hello.getId());
    }

}
