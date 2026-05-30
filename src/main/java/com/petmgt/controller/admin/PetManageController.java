package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.service.FileStorageService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pets")
public class PetManageController {

    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final FileStorageService fileStorageService;

    public PetManageController(PetMapper petMapper, BreedMapper breedMapper,
                                PetImageMapper petImageMapper,
                                FileStorageService fileStorageService) {
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Pet>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long breedId,
            @RequestParam(required = false) String status) {
        Page<Pet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like(Pet::getName, name);
        if (breedId != null) wrapper.eq(Pet::getBreedId, breedId);
        if (status != null && !status.isEmpty()) wrapper.eq(Pet::getStatus, status);
        wrapper.orderByDesc(Pet::getCreatedAt);
        Page<Pet> result = petMapper.selectPage(pageParam, wrapper);
        for (Pet p : result.getRecords()) {
            Breed b = breedMapper.selectById(p.getBreedId());
            if (b != null) p.setBreedName(b.getBreedName());
        }
        PageResponse<Pet> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<Void> create(
            @RequestParam("name") String name,
            @RequestParam("breedId") Long breedId,
            @RequestParam("gender") String gender,
            @RequestParam("age") Integer age,
            @RequestParam(value = "weight", required = false) Double weight,
            @RequestParam("healthStatus") String healthStatus,
            @RequestParam(value = "vaccineStatus", required = false) String vaccineStatus,
            @RequestParam(value = "sterilizationStatus", required = false) String sterilizationStatus,
            @RequestParam("personality") String personality,
            @RequestParam(value = "adoptionRequirement", required = false) String adoptionRequirement,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "coverIndex", defaultValue = "0") Integer coverIndex) throws IOException {

        Pet pet = new Pet();
        pet.setName(name);
        pet.setBreedId(breedId);
        pet.setGender(gender);
        pet.setAge(age);
        pet.setWeight(weight != null ? new BigDecimal(weight) : null);
        pet.setHealthStatus(healthStatus);
        pet.setVaccineStatus(vaccineStatus);
        pet.setSterilizationStatus(sterilizationStatus);
        pet.setPersonality(personality);
        pet.setAdoptionRequirement(adoptionRequirement);
        pet.setStatus("available");
        pet.setCreatedBy(SecurityUtil.getCurrentUser().getId());
        petMapper.insert(pet);

        if (images != null && !images.isEmpty()) {
            saveImages(pet.getId(), images, coverIndex);
        }
        return ApiResponse.success("发布成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("breedId") Long breedId,
            @RequestParam("gender") String gender,
            @RequestParam("age") Integer age,
            @RequestParam(value = "weight", required = false) Double weight,
            @RequestParam("healthStatus") String healthStatus,
            @RequestParam(value = "vaccineStatus", required = false) String vaccineStatus,
            @RequestParam(value = "sterilizationStatus", required = false) String sterilizationStatus,
            @RequestParam("personality") String personality,
            @RequestParam(value = "adoptionRequirement", required = false) String adoptionRequirement,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "coverIndex", defaultValue = "0") Integer coverIndex,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds) throws IOException {

        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return ApiResponse.error(404, "宠物不存在");
        }
        pet.setName(name);
        pet.setBreedId(breedId);
        pet.setGender(gender);
        pet.setAge(age);
        pet.setWeight(weight != null ? new BigDecimal(weight) : null);
        pet.setHealthStatus(healthStatus);
        pet.setVaccineStatus(vaccineStatus);
        pet.setSterilizationStatus(sterilizationStatus);
        pet.setPersonality(personality);
        pet.setAdoptionRequirement(adoptionRequirement);
        if (status != null) pet.setStatus(status);
        petMapper.updateById(pet);

        if (deleteImageIds != null) {
            for (Long imageId : deleteImageIds) {
                PetImage img = petImageMapper.selectById(imageId);
                if (img != null) {
                    fileStorageService.delete(img.getImageUrl());
                    petImageMapper.deleteById(imageId);
                }
            }
        }
        if (images != null && !images.isEmpty()) {
            saveImages(id, images, coverIndex);
        }
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        for (PetImage img : images) {
            fileStorageService.delete(img.getImageUrl());
            petImageMapper.deleteById(img.getId());
        }
        petMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    private void saveImages(Long petId, List<MultipartFile> images, Integer coverIndex) throws java.io.IOException {
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file.isEmpty()) continue;
            String filename = fileStorageService.store(file);
            PetImage petImage = new PetImage();
            petImage.setPetId(petId);
            petImage.setImageUrl(filename);
            petImage.setIsCover(i == coverIndex ? 1 : 0);
            petImageMapper.insert(petImage);
        }
    }
}
