package study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCond {

    private String username;
    private String teamname;
    private Integer ageGoe;
    private Integer ageLoe;

}
