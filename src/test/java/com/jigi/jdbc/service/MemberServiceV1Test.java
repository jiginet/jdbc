package com.jigi.jdbc.service;

import com.jigi.jdbc.domain.Member;
import com.jigi.jdbc.repository.MemberRepositoryV1;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static com.jigi.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

class MemberServiceV1Test {

    public static final String MemberA = "memberA";
    public static final String MemberB = "memberB";
    public static final String MemberEX = "ex";

    private MemberServiceV1 memberService;
    private MemberRepositoryV1 memberRepository;

    @BeforeEach
    void setUp() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        memberRepository.delete(MemberA);
        memberRepository.delete(MemberB);
        memberRepository.delete(MemberEX);
    }

    @Test
    @DisplayName("정상이체")
    void normal() throws SQLException {
        // given
        Member memberA = new Member(MemberA, 1_000);
        Member memberB = new Member(MemberB, 1_000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 500);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(500);
        assertThat(findMemberB.getMoney()).isEqualTo(1_500);
    }

    @Test
    @DisplayName("이체오류")
    void abnormal() throws SQLException {
        // given
        Member memberA = new Member(MemberA, 1_000);
        Member memberEx = new Member(MemberEX, 1_000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        Assertions.assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 500))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member fineMemberEx = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(500);
        assertThat(fineMemberEx.getMoney()).isEqualTo(1_000);
    }
}