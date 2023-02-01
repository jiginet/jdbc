package com.jigi.jdbc.exception.translator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.jigi.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionTest() {
        String sql = "select bad grammer";

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            assertThat(errorCode).isEqualTo(42_122);
            log.info("errorCode = {}", errorCode);
            log.info("db error", e);
        }
    }

    @Test
    void springExceptionTranslatorTest() {
        String sql = "select bad grammar";

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {

            SQLErrorCodeSQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException accessException = translator.translate("select test", sql, e);

            log.info("accessException", accessException);
            assertThat(accessException.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
