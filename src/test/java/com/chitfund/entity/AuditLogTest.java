package com.chitfund.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogTest {

    @Test
    void auditLogMutationLifecycleThrows() throws Exception {
        AuditLog auditLog = new AuditLog();
        Method method = AuditLog.class.getDeclaredMethod("preventMutation");
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(auditLog))
                .hasRootCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Audit logs are immutable");
    }
}
