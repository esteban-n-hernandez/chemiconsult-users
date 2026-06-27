package com.chemiconsult.mapper;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.to.UserTO;

import java.util.List;

public class UserMapper {

    public static UserDE mapUserToEntity(UserTO userTO) {

        UserDE userDE = new UserDE();
        userDE.setId(userTO.getId());
        userDE.setUsername(userTO.getUsername());
        userDE.setEmail(userTO.getEmail());
        userDE.setRol(userTO.getRol());

        return userDE;
    }

    public static UserTO mapEntityToUserTO(UserDE userDE) {
        return UserTO.builder()
                .id(userDE.getId())
                .username(userDE.getUsername())
                .email(userDE.getEmail())
                .rol(userDE.getRol())
                .build();
    }

    public static List<UserTO> mapEntityToUserTOList(List<UserDE> userDEList) {
        return userDEList.stream()
                .map(UserMapper::mapEntityToUserTO)
                .toList();
    }


}
