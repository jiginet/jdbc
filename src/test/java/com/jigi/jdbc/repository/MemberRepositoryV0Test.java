package com.jigi.jdbc.repository;

import com.jigi.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {

        // save
        Member member = new Member("jigi5", 1_000_000);
        repository.save(member);

        // findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("찾은회원 = {}", findMember);
        assertThat(findMember).isEqualTo(member);

        // update
        int newMoney = 2_000_000;
        repository.update(member.getMemberId(), newMoney);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(newMoney);

        // delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}