package com.shopsphere.authservice.mapper;

import com.shopsphere.authservice.dto.response.RegisterResponse;
import com.shopsphere.authservice.entity.AuthUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {

    RegisterResponse toRegisterResponse(AuthUser authUser);
}
