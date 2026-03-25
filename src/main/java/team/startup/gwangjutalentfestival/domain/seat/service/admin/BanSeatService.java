package team.startup.gwangjutalentfestival.domain.seat.service.admin;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;

public interface BanSeatService {
    void execute(BanSeatRequest request);
}
