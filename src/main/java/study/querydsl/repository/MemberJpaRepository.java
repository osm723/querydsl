package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }


    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findAll_querydsl() {
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByName_querydsl(String username) {
        return queryFactory.selectFrom(member).where(member.username.eq(username)).fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCond memberSearchCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(memberSearchCond.getUsername())) {
            builder.and(member.username.eq(memberSearchCond.getUsername()));
        }
        if (StringUtils.hasText(memberSearchCond.getTeamname())) {
            builder.and(team.name.eq(memberSearchCond.getTeamname()));
        }
        if (memberSearchCond.getAgeGoe() != null) {
            builder.and(member.age.goe(memberSearchCond.getAgeGoe()));
        }
        if (memberSearchCond.getAgeLoe() != null) {
            builder.and(member.age.loe(memberSearchCond.getAgeLoe()));
        }


        return queryFactory
                .select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"), team.name.as("teamname")))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

}
