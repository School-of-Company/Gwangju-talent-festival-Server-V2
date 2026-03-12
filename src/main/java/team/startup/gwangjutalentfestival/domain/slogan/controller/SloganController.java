package team.startup.gwangjutalentfestival.domain.slogan.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.startup.gwangjutalentfestival.domain.slogan.dto.request.CreateSloganRequest;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.service.CreateSloganService;

@RestController
@RequestMapping("/slogan")
@RequiredArgsConstructor
public class SloganController {

    private final CreateSloganService createSloganService;

    @PostMapping
    public ResponseEntity<SloganEntity> createSlogan(@Valid @RequestBody CreateSloganRequest request) {
        createSloganService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}