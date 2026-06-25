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
        UserTO userTO = new UserTO();
        userTO.setId(userDE.getId());
        userTO.setUsername(userDE.getUsername());
        userTO.setEmail(userDE.getEmail());
        userTO.setRol(userDE.getRol());

        return userTO;
    }

    public static List<UserTO> mapEntityToUserTOList(List<UserDE> userDEList) {
        return userDEList.stream()
                .map(UserMapper::mapEntityToUserTO)
                .toList();
    }


}
