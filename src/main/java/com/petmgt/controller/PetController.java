package com.petmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.dto.PetSearchCriteria;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;
    private final BreedMapper breedMapper;

    public PetController(PetService petService, BreedMapper breedMapper) {
        this.petService = petService;
        this.breedMapper = breedMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<Pet>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            PetSearchCriteria criteria) {
        Page<Pet> pageParam = new Page<>(page, size);
        Page<Pet> result = petService.findPets(pageParam, criteria);
        PageResponse<Pet> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Pet> detail(@PathVariable Long id) {
        Pet pet = petService.findPetDetail(id);
        if (pet == null) {
            return ApiResponse.error(404, "宠物不存在");
        }
        return ApiResponse.success(pet);
    }

    @GetMapping("/{id}/images")
    public ApiResponse<List<PetImage>> images(@PathVariable Long id) {
        List<PetImage> images = petService.findPetImages(id);
        return ApiResponse.success(images);
    }
}
