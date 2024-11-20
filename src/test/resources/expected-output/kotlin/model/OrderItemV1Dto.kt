package org.github.torand.test.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "OrderItemV1", description = "An order item")
@JvmRecord
data class OrderItemV1Dto (

    @field:Schema(description = "The product ordered", required = true)
    @field:Valid
    @field:NotNull
    val product: ProductV1Dto,

    @field:Schema(description = "The quantity ordered", required = true)
    @field:NotNull
    @field:Min(1)
    val quantity: Int
)
