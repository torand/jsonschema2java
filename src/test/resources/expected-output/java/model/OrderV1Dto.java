package io.github.torand.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "OrderV1", description = "An order placed in the web shop")
public record OrderV1Dto (

    @Schema(description = "Owner of the order", required = true)
    @Valid
    @NotNull
    UserV1Dto placedBy,

    @Schema(description = "Status of the order", required = true)
    @NotNull
    OrderStatusV1Dto status,

    @Schema(description = "Date and time of order creation", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description = "Order items", required = true)
    @Valid
    @NotNull
    @Size(min = 1)
    List<@NotNull OrderItemV1Dto> items,

    @Schema(description = "Additional comment from customer")
    String comment
) {

}
