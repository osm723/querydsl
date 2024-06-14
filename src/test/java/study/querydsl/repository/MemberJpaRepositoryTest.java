package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.QuerydslApplication;
import study.querydsl.entity.Team;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository repository;

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

        List<Member> findMemberA = repository.findByName("memberA");
        assertThat(findMemberA).containsExactly(member);
    }

    @Test
    void basicTest2() {
        Member member = new Member("memberA", 10, null);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> findAllMember = repository.findAll_querydsl();
        assertThat(findAllMember).containsExactly(member);

        List<Member> findMemberA = repository.findByName_querydsl("memberA");
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

        List<MemberTeamDto> memberTeamDtos = repository.searchByBuilder(memberSearchCond);

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

        List<MemberTeamDto> memberTeamDtos = repository.searchByBuilder(memberSearchCond);

        assertThat(memberTeamDtos).extracting("username").containsExactly("memberC", "memberD");
    }
}