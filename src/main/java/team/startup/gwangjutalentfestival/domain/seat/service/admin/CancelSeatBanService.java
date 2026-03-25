package team.startup.gwangjutalentfestival.domain.seat.service.admin;

import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;

public interface CancelSeatBanService {
    void execute(CancelSeatBanRequest request);
}
