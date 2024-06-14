package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository repository;

    private final MemberRepository memberRepository;


    @GetMapping("/v1/members")
    public List<MemberTeamDto> members(MemberSearchCond memberSearchCond) {
        return repository.searchByWhere(memberSearchCond);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCond memberSearchCond, Pageable pageable) {
        return memberRepository.searchPageSimple(memberSearchCond, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCond memberSearchCond, Pageable pageable) {
        return memberRepository.searchPageComplex(memberSearchCond, pageable);
    }
}
