package com.nagpal.shivam.workout_manager_user.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class BaseDocument {
    @JsonProperty("_id")
    private String id;
    private Long timeCreated;
    private Long timeLastModified;
}
