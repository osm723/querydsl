package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<MemberTeamDto> search(MemberSearchCond memberSearchCond) {
        return queryFactory
                .select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"), team.name.as("teamname")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(memberSearchCond.getUsername()),
                        teamnameEq(memberSearchCond.getTeamname()),
                        ageLoe(memberSearchCond.getAgeLoe()),
                        ageGoe(memberSearchCond.getAgeGoe()))
                .fetch();
    }

    public Page<MemberTeamDto> searchPageSimple(MemberSearchCond memberSearchCond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                    .select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"), team.name.as("teamname")))
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(usernameEq(memberSearchCond.getUsername()),
                            teamnameEq(memberSearchCond.getTeamname()),
                            ageLoe(memberSearchCond.getAgeLoe()),
                            ageGoe(memberSearchCond.getAgeGoe()))
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetchResults();
        List<MemberTeamDto> contents = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(contents, pageable, total);
    }

    public Page<MemberTeamDto> searchPageComplex(MemberSearchCond memberSearchCond, Pageable pageable) {
        List<MemberTeamDto> result = getSearchPageList(memberSearchCond, pageable);

        JPAQuery<Member> totalQuery = getSearchPageSize(memberSearchCond);

        //return new PageImpl<>(result, pageable, total);
        return PageableExecutionUtils.getPage(result, pageable, totalQuery::fetchCount);
    }

    private JPAQuery<Member> getSearchPageSize(MemberSearchCond memberSearchCond) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(memberSearchCond.getUsername()),
                        teamnameEq(memberSearchCond.getTeamname()),
                        ageLoe(memberSearchCond.getAgeLoe()),
                        ageGoe(memberSearchCond.getAgeGoe()))
                ;
    }

    private List<MemberTeamDto> getSearchPageList(MemberSearchCond memberSearchCond, Pageable pageable) {
        return queryFactory
                .select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"), team.name.as("teamname")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(memberSearchCond.getUsername()),
                        teamnameEq(memberSearchCond.getTeamname()),
                        ageLoe(memberSearchCond.getAgeLoe()),
                        ageGoe(memberSearchCond.getAgeGoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }


    private BooleanExpression ageGoe(Integer age) {
        return age != null ? member.age.goe(age) : null;
    }

    private BooleanExpression ageLoe(Integer age) {
        return age != null ? member.age.loe(age) : null;
    }

    private BooleanExpression teamnameEq(String teamname) {
        return StringUtils.hasText(teamname) ? team.name.eq(teamname) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }
}
