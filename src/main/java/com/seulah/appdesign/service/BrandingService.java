package com.seulah.appdesign.service;


import com.seulah.appdesign.entity.*;
import com.seulah.appdesign.repository.*;
import com.seulah.appdesign.request.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;


@Service
public class BrandingService {
    private final BrandingRepository brandingRepository;

    private final BrandLogoService brandLogoService;


    private final BrandSplashScreenRepository brandSplashScreenRepository;

    private final BrandColorRepository brandColorRepository;

    private final BrandingLayoutService brandingLayoutService;
    private final BrandingLayoutRepository brandingLayoutRepository;

    private final BrandLogoRepository brandLogoRepository;

    public BrandingService(BrandingRepository brandingRepository, BrandLogoService brandLogoService, BrandSplashScreenRepository brandSplashScreenRepository, BrandColorRepository brandColorRepository, BrandingLayoutService brandingLayoutService, BrandingLayoutRepository brandingLayoutRepository, BrandLogoRepository brandLogoRepository) {
        this.brandingRepository = brandingRepository;
        this.brandLogoService = brandLogoService;
        this.brandSplashScreenRepository = brandSplashScreenRepository;
        this.brandColorRepository = brandColorRepository;
        this.brandingLayoutService = brandingLayoutService;
        this.brandingLayoutRepository = brandingLayoutRepository;
        this.brandLogoRepository = brandLogoRepository;
    }

    public ResponseEntity<MessageResponse> saveBranding(String brandingName) {
        Branding branding = brandingRepository.findByBrandName(brandingName);
        if (branding != null) {
            return new ResponseEntity<>(new MessageResponse("Brand Name Already Exist", null, false), HttpStatus.CONFLICT);
        }
        branding = new Branding();
        branding.setBrandName(brandingName);
        branding = brandingRepository.save(branding);
        return new ResponseEntity<>(new MessageResponse("Successfully Created App Design", branding, false), HttpStatus.CREATED);
    }

    public ResponseEntity<MessageResponse> getBrandingById(String id) {
        Optional<Branding> appDesign = brandingRepository.findById(id);
        return appDesign.map(design -> new ResponseEntity<>(new MessageResponse("Success", design, false), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(new MessageResponse("No Record Found", null, false), HttpStatus.OK));

    }

    public ResponseEntity<MessageResponse> getAll() {
        List<Branding> brandingList = brandingRepository.findAll();
        return new ResponseEntity<>(new MessageResponse("Success", brandingList, false), HttpStatus.OK);
    }

    public ResponseEntity<MessageResponse> deleteById(String id) {
        Optional<Branding> appDesign = brandingRepository.findById(id);
        if (appDesign.isPresent()) {
            brandingRepository.delete(appDesign.get());
            return new ResponseEntity<>(new MessageResponse("Success", null, false), HttpStatus.OK);
        }

        return new ResponseEntity<>(new MessageResponse("No Record Found", null, false), HttpStatus.OK);
    }

    public ResponseEntity<MessageResponse> updateById(String id, String brandName) {
        Optional<Branding> appDesignOptional = brandingRepository.findById(id);
        if (appDesignOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("No Record Found Against this Id", null, false), HttpStatus.OK);
        }
        Branding branding = appDesignOptional.get();
        if (brandName != null && !brandName.isEmpty()) {
            branding.setBrandName(brandName);
        }

        branding = brandingRepository.save(branding);
        return new ResponseEntity<>(new MessageResponse("Successfully Updated", branding, false), HttpStatus.OK);
    }

    public ResponseEntity<BrandDetailResponse> getBrandDetail(String brandId) throws IOException {
        Optional<Branding> branding = brandingRepository.findById(brandId);
        Optional<BrandingSplashScreen> brandingSplashScreen = brandSplashScreenRepository.findByBrandId(brandId);
        Optional<BrandingColor> brandingColor = brandColorRepository.findByBrandId(brandId);
        BrandingLogo brandingLogos = brandLogoRepository.findByBrandId(brandId);
        List<BrandingLayout> brandingLayouts = brandingLayoutRepository.findAllByBrandId(brandId);

        byte[] brandLogo = brandLogoService.getLogoFileUrlByBrandId(brandId);
        List<LayoutDetail> layoutDetails = new ArrayList<>();
        List<byte[]> iconContents = brandingLayoutService.getIconByBrandId(brandId);
        List<byte[]> lottieContents = brandingLayoutService.getLottieByBrandId(brandId);

        int size = Math.min(iconContents.size(), lottieContents.size());
        for (int i = 0; i < size; i++) {
            LayoutDetail layoutDetail = new LayoutDetail();
            layoutDetail.setBrandId(brandingLayouts.get(i).getBrandId());
            layoutDetail.setIconContent(iconContents.get(i));
            layoutDetail.setLottieContent(lottieContents.get(i));
            layoutDetails.add(layoutDetail);
        }

        BrandDetailResponse brandDetailResponse = new BrandDetailResponse(
                brandingLogos.getBrandId(),
                branding.orElse(null),
                brandingSplashScreen.orElse(null),
                brandingColor.orElse(null),
                brandLogo,
                layoutDetails
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(brandDetailResponse);
    }


    public ResponseEntity<BrandDetailResponse> getBrandDetailByBrandName(String brandName) throws IOException {
        Branding branding = brandingRepository.findByBrandName(brandName);
        if (branding != null) {
            return getBrandDetail(branding.getId());
        }
        return null;
    }
}
