package team.startup.gwangjutalentfestival.auth.service;

import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.oauth.data.MemberCommand;

public interface MemberRegistrationService {
    UserEntity findOrRegister(MemberCommand command);
}