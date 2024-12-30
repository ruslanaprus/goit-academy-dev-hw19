package com.example.notemanager.api.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@JsonIgnoreProperties({"pageable"})
public class RestResponsePage<T> extends PageImpl<T> {
    @JsonCreator
    public RestResponsePage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("last") boolean last,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("sort") Object sort,
            @JsonProperty("first") boolean first,
            @JsonProperty("numberOfElements") int numberOfElements
    ) {
        super(content, Pageable.unpaged(), totalElements);
    }

    public RestResponsePage() {
        super(List.of());
    }
}