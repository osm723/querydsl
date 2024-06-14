package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.QuerydslApplication;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory query;

    @BeforeEach
    void querydslHello() {
        query = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 20, teamA);
        Member memberC = new Member("memberC", 30, teamB);
        Member memberD = new Member("memberD", 40, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
    }

    @Test
    void startJPQL() {
        Member findMember = em.createQuery("select m from Member m where m.username = :name", Member.class)
                .setParameter("name", "memberA")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("memberA");
    }

    @Test
    void startQuerydsl() {
        //JPAQueryFactory query = new JPAQueryFactory(em);
        //QMember m = new QMember("m");
        QMember m = member;

        Member findMember = query
                .select(m)
                .from(m)
                .where(m.username.eq("memberA"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("memberA");
    }

    @Test
    void startQuerydsl2() {
        Member findMember = query
                .select(member)
                .from(member)
                .where(member.username.eq("memberA"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("memberA");
    }

    @Test
    void search() {
        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("memberA").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("memberA");
    }

    @Test
    void searchAndParam() {
        Member findMember = query
                .selectFrom(member)
                .where(
                        member.username.eq("memberA"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("memberA");
    }

    @Test
    void fetchTest() {
        List<Member> fetch = query
                .selectFrom(member)
                .fetch();
    }

    @Test
    void fecthOneTest() {
        Member fetchOne = query
                .selectFrom(member)
                .where(member.username.eq("memberA"))
                .fetchOne();
    }

    @Test
    void fetchFetchFirstTest() {
        Member fetchFirst = query
                .selectFrom(member)
                .fetchFirst();
    }

    @Test
    void fetchResultTest() {
        QueryResults<Member> memberQueryResults = query
                .selectFrom(member)
                .fetchResults();

        memberQueryResults.getLimit();
        memberQueryResults.getOffset();
        memberQueryResults.getTotal();
        List<Member> results = memberQueryResults.getResults();
    }

    @Test
    void fetchCountTest() {
        long fetchCount = query
                .selectFrom(member)
                .fetchCount();
    }

    @Test
    void sort() {
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        em.persist(new Member(null, 100));

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void page01() {
        List<Member> result = query
                .selectFrom(member)
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void page02() {
        QueryResults<Member> memberQueryResults = query
                .selectFrom(member)
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(memberQueryResults.getLimit()).isEqualTo(2);
        assertThat(memberQueryResults.getOffset()).isEqualTo(1);
        assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    void aggregation() {
        List<Tuple> fetch = query
                .select(member.count(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void group() {
        List<Tuple> fetch = query
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);


        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void join() {
        List<Member> result = query
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result).extracting("username").contains("memberA", "memberB");
    }

    @Test
    void crossJoin() {
        em.persist(new Member("teamA",50));
        em.persist(new Member("teamB",50));
        em.persist(new Member("teamC",50));

        List<Member> result = query
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result).extracting("username").contains("teamA","teamB");
    }

    @Test
    void join_on_filtering() {
        List<Tuple> tuples = query
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void join_on_no_relation() {
        em.persist(new Member("teamA",50));
        em.persist(new Member("teamB",50));
        em.persist(new Member("teamC",50));

        List<Tuple> tuples = query
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNotUse() {
        em.flush();
        em.clear();

        Member findMember = query
                .select(member)
                .from(member)
                .where(member.username.eq("memberA"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = query
                .select(member)
                .from(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("memberA"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인 적용").isTrue();
    }

    @Test
    void subQuery() {
        QMember subMember = new QMember("subMember");

        List<Member> result = query
                .select(member)
                .from(member)
                .where(member.age.eq(
                        select(subMember.age.max())
                                .from(subMember)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(40);
    }

    @Test
    void subQueryGoe() {
        QMember subMember = new QMember("subMember");

        List<Member> result = query
                .select(member)
                .from(member)
                .where(member.age.goe(
                        select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(30,40);
    }

    @Test
    void subQueryIn() {
        QMember subMember = new QMember("subMember");

        List<Member> result = query
                .select(member)
                .from(member)
                .where(member.age.in(
                        select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(20,30,40);
    }

    @Test
    void subQuerySelect() {
        QMember subMember = new QMember("subMember");

        List<Tuple> result = query
                .select(member.username, select(subMember.age.avg())
                        .from(subMember))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }


    @Test
    void caseQuery() {
        List<String> result = query
                .select(member.age
                        .when(10).then("10대")
                        .when(20).then("20대")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void complexCaseQeury() {
        List<String> result = query
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("10대")
                        .when(member.age.between(21, 30)).then("20대")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void constant() {
        List<Tuple> result = query
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void concat() {
        List<String> result = query
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void resultOne() {
        List<String> result = query
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void resultTuple() {
        List<Tuple> result = query
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
        }
    }

    @Test
    void findMemberDtoByJpql() {
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                        "from Member m", MemberDto.class)
                .getResultList();
        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findMemberDtoBySetter() {
        List<MemberDto> result = query
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findMemberDtoByField() {
        List<MemberDto> result = query
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findMemberDtoByConstructor() {
        List<MemberDto> result = query
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findUserDtoBySetter() {
        List<UserDto> result = query
                .select(Projections.bean(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    void findUserDtoByField() {
        List<UserDto> result = query
                .select(Projections.fields(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    void findUserDtoByConstructor() {
        List<UserDto> result = query
                .select(Projections.constructor(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    void findUserDtoSubBySetter() {
        QMember subMember = new QMember("subMember");
        List<UserDto> result = query
                .select(Projections.bean(UserDto.class, member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(subMember.age.max())
                                .from(subMember), "age")
                                )
                        )
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    void findMemberDtoByQueryProjection() {
        List<MemberDto> result = query
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void dynamic_booleanBuilder() {
        String username = "memberA";
        Integer age = 10;

        List<Member> result = selectMember1Query(username, age);

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    private List<Member> selectMember1Query(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return query
                .select(member)
                .from(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamic_where() {
        String username = "memberA";
        Integer age = null;

        List<Member> result = selectMember2Query(username, age);

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    private List<Member> selectMember2Query(String usernameCond, Integer ageCond) {
        return query
                .select(member)
                .from(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                //.where(allEq(usernameCond,ageCond))
                .fetch();
    }

    @Test
    @Commit
    void updateQuery() {
        long executed = query
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(26))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = query
                .selectFrom(member)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    @Commit
    void updateAdd() {
        query
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = query
                .selectFrom(member)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    @Commit
    void updateMultiple() {
        query
                .update(member)
                .set(member.age, member.age.multiply(2))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = query
                .selectFrom(member)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    void deleteQuery() {
        query
                .delete(member)
                .where(member.age.lt(26))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = query
                .selectFrom(member)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    void functionReplace() {
        List<String> result = query
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
                        member.username,
                        "member",
                        "M"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void functionUpper() {
        List<String> result = query
                .select(Expressions.stringTemplate("function('upper', {0})",
                        member.username,
                        "member"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void functionUpper_jpa() {
        List<String> result = query
                .select(member.username.upper())
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


}
