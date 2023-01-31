package com.jigi.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void unCheckedException() {

        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request()).isInstanceOf(RuntimeException.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class Repository {
        public void call() throws RuntimeSQLException {
            try {
                runSQL();
            } catch (SQLException e) {
                log.info("SQL 실행오류, message={}", e.getMessage(), e);
                throw new RuntimeSQLException(e);
            }
        }

        private void runSQL() throws SQLException {
            throw new SQLException("ex");
        }

    }

    static class NetworkClient {
        public void call() throws RuntimeConnectionException {

            try {
                connectNetwork();
            } catch (ConnectException e) {
                log.info("네트워크 연결오류, message={}", e.getMessage(), e);
                throw new RuntimeConnectionException(e);
            }
        }

        private void connectNetwork() throws ConnectException {
            throw new ConnectException("ex");
        }
    }

    static class RuntimeConnectionException extends RuntimeException {
        public RuntimeConnectionException(Throwable cause) {
            super(cause);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
