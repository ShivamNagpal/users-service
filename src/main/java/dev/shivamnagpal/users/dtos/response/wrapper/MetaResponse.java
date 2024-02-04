package dev.shivamnagpal.users.dtos.response.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.shivamnagpal.users.enums.ResponseStatus;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetaResponse(ResponseStatus status, Long timestamp) {

}
