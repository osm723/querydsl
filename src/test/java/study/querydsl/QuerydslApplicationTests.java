package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import entity.Hello;
import entity.QHello;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class QuerydslApplicationTests {

	@Test
	void contextLoads() {
	}

}
