package com.jigi.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedTest {

    @Test
    void unChecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unChecked_throw() {
        Service service = new Service();
        Assertions.assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUnCheckedException.class);
    }

    /**
     * RuntimeException을 상속받은 예외는 unChecked 예외가 된다.
     */
    static class MyUnCheckedException extends RuntimeException {
        public MyUnCheckedException(String message) {
            super(message);
        }
    }

    static class Repository {
        public void call() {
            throw new MyUnCheckedException("ex");
        }
    }

    static class Service {
        Repository repository = new Repository();

         public void callCatch() {
             try {
                 repository.call();
             } catch (MyUnCheckedException e) {
                 log.info("예외처리, message={}", e.getMessage(), e);
             }
         }

        public void callThrow() {
             repository.call();
        }
    }
}
