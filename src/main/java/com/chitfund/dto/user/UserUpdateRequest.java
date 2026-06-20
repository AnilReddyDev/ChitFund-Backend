package com.chitfund.dto.user;

import com.chitfund.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotNull
    private Role role;
}
