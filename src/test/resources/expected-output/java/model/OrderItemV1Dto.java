package io.github.torand.test.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "OrderItemV1", description = "An order item")
public record OrderItemV1Dto (

    @Schema(description = "The product ordered", required = true)
    @Valid
    @NotNull
    ProductV1Dto product,

    @Schema(description = "The quantity ordered", required = true)
    @NotNull
    @Min(1)
    Integer quantity
) {

}
