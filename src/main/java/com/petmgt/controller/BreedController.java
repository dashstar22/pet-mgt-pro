package com.petmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.Breed;
import com.petmgt.mapper.BreedMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("publicBreedController")
@RequestMapping("/api/breeds")
public class BreedController {

    private final BreedMapper breedMapper;

    public BreedController(BreedMapper breedMapper) {
        this.breedMapper = breedMapper;
    }

    @GetMapping
    public ApiResponse<List<Breed>> list(@RequestParam(required = false) String petType) {
        LambdaQueryWrapper<Breed> wrapper = new LambdaQueryWrapper<>();
        if (petType != null && !petType.isEmpty()) {
            wrapper.eq(Breed::getPetType, petType);
        }
        List<Breed> breeds = breedMapper.selectList(wrapper);
        return ApiResponse.success(breeds);
    }
}
