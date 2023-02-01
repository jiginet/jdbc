package com.jigi.jdbc.exception.translator;

import com.jigi.jdbc.domain.Member;
import com.jigi.jdbc.exception.MyDBException;
import com.jigi.jdbc.exception.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static com.jigi.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test {

    Service service;
    Repository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeyTest() {
        service.create("hello");
        service.create("hello");
    }

    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                log.info("saveId = {}", memberId);
            } catch (MyDuplicateKeyException e) {
                log.info("아이디 중복 = {}", memberId);
                String retryId = getRandomId(memberId);
                repository.save(new Member(retryId, 0));
                log.info("retryId = {}", retryId);
            } catch (MyDBException e) {
                log.info("DB 에러", e);
                throw e;
            }
        }

        private static String getRandomId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }
    }

    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?, ?)";

            Connection conn = null;
            PreparedStatement pstmt = null;

            try {
                conn = dataSource.getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDBException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(conn);
            }
        }
    }
}
