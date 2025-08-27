package com.example.k5_iot_springboot.dto.G_User.request;

import com.example.k5_iot_springboot.common.enums.RoleType;
import jakarta.validation.constraints.NotNull;

public record RoleModifyRequest(
        @NotNull
        RoleType role
) {}
