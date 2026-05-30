package com.petmgt.controller.admin;

import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Breed;
import com.petmgt.service.BreedService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/breeds")
public class BreedController {

    private final BreedService breedService;

    public BreedController(BreedService breedService) {
        this.breedService = breedService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Breed>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String petType) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Breed> result =
                breedService.list(page, size, petType);
        PageResponse<Breed> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Breed breed) {
        breedService.save(breed);
        return ApiResponse.success("创建成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Breed breed) {
        breed.setId(id);
        breedService.update(breed);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        breedService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
