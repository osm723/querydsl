package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository repository;

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory query;

    @Test
    void basicTest() {
        Member member = new Member("memberA", 10, null);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> findAllMember = repository.findAll();
        assertThat(findAllMember).containsExactly(member);

        List<Member> findMemberA = repository.findByUsername("memberA");
        assertThat(findMemberA).containsExactly(member);
    }

    @Test
    void searchTest() {
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

        MemberSearchCond memberSearchCond = new MemberSearchCond();
        memberSearchCond.setAgeGoe(35);
        memberSearchCond.setAgeLoe(40);
        memberSearchCond.setTeamname("teamB");

        List<MemberTeamDto> memberTeamDtos = repository.search(memberSearchCond);

        assertThat(memberTeamDtos).extracting("username").containsExactly("memberD");
    }

    @Test
    void searchTest2() {
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

        MemberSearchCond memberSearchCond = new MemberSearchCond();
        memberSearchCond.setAgeGoe(30);
        //memberSearchCond.setAgeLoe(40);
        memberSearchCond.setTeamname("teamB");

        List<MemberTeamDto> memberTeamDtos = repository.search(memberSearchCond);

        assertThat(memberTeamDtos).extracting("username").containsExactly("memberC", "memberD");
    }

    @Test
    void searchPageSimpleTest() {
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

        MemberSearchCond memberSearchCond = new MemberSearchCond();
        //memberSearchCond.setAgeGoe(30);
        //memberSearchCond.setAgeLoe(40);
        //memberSearchCond.setTeamname("teamB");

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberTeamDtos = repository.searchPageSimple(memberSearchCond, pageRequest);

        assertThat(memberTeamDtos).extracting("username").containsExactly("memberA", "memberB", "memberC");
    }

    @Test
    void searchPageComplexTest() {
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

        MemberSearchCond memberSearchCond = new MemberSearchCond();
        //memberSearchCond.setAgeGoe(30);
        //memberSearchCond.setAgeLoe(40);
        //memberSearchCond.setTeamname("teamB");

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberTeamDtos = repository.searchPageComplex(memberSearchCond, pageRequest);

        assertThat(memberTeamDtos).extracting("username").containsExactly("memberA", "memberB", "memberC");
    }

    @Test
    void querydslPredicateExecutorTest() {
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

        QMember member = QMember.member;
        Iterable<Member> result = repository.findAll(member.age.between(10, 40).and(member.username.eq("memberB")));

        for (Member findMember : result) {
            System.out.println("findMember = " + findMember);
        }
    }


}