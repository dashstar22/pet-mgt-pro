package com.petmgt.controller;

import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.Pet;
import com.petmgt.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    private final PetService petService;

    public HomeController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/api/home")
    public ApiResponse<List<Pet>> home() {
        List<Pet> latestPets = petService.findLatestPets(8);
        return ApiResponse.success(latestPets);
    }
}
