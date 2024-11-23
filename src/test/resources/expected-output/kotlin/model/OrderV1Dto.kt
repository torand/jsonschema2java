package io.github.torand.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "OrderV1", description = "An order placed in the web shop")
@JvmRecord
data class OrderV1Dto (

    @field:Schema(description = "Owner of the order", required = true)
    @field:Valid
    @field:NotNull
    val placedBy: UserV1Dto,

    @field:Schema(description = "Status of the order", required = true)
    @field:NotNull
    val status: OrderStatusV1Dto,

    @field:Schema(description = "Date and time of order creation", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdTime: LocalDateTime,

    @field:Schema(description = "Order items", required = true)
    @field:Valid
    @field:NotNull
    @field:Size(min = 1)
    val items: List<@NotNull OrderItemV1Dto>,

    @field:Schema(description = "Additional comment from customer")
    val comment: String? = null
)
