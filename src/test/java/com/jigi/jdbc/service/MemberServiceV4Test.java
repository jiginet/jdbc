package com.jigi.jdbc.service;

import com.jigi.jdbc.domain.Member;
import com.jigi.jdbc.repository.MemberRepository;
import com.jigi.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class MemberServiceV4Test {

    public static final String MemberA = "memberA";
    public static final String MemberB = "memberB";
    public static final String MemberEX = "ex";

    @Autowired
    private MemberServiceV4 memberService;

    @Autowired
    private MemberRepository memberRepository;

    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberService() {
            return new MemberServiceV4(memberRepository());
        }
    }

    @BeforeEach
    void setUp() {
        memberRepository.delete(MemberA);
        memberRepository.delete(MemberB);
        memberRepository.delete(MemberEX);
    }

    @Test
    @DisplayName("정상이체")
    void normal() {
        // given
        Member memberA = new Member(MemberA, 1_000);
        Member memberB = new Member(MemberB, 1_000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        log.info("Start TX ===");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 500);
        log.info("Finish TX ===");

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(500);
        assertThat(findMemberB.getMoney()).isEqualTo(1_500);
    }

    @Test
    @DisplayName("이체오류")
    void abnormal() {
        // given
        Member memberA = new Member(MemberA, 1_000);
        Member memberEx = new Member(MemberEX, 1_000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        Assertions.assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 500)).isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member fineMemberEx = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(1_000);
        assertThat(fineMemberEx.getMoney()).isEqualTo(1_000);
    }
}