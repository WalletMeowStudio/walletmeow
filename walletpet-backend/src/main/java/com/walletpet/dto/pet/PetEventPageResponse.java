package com.walletpet.dto.pet;

import java.util.List;

import lombok.Data;

@Data
public class PetEventPageResponse {

    private List<PetEventResponse> items;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;
}