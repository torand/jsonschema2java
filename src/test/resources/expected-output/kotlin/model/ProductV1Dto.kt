package io.github.torand.test.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.torand.test.serialization.ProductNoSerializer
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "ProductV1", description = "A product available in the web shop")
@JvmRecord
data class ProductV1Dto (

    @field:Schema(description = "Product number", required = true)
    @field:NotBlank
    @field:JsonSerialize(using = ProductNoSerializer::class)
    val number: String,

    @field:Schema(description = "Product name", required = true)
    @field:NotBlank
    @field:Size(min = 3)
    val name: String,

    @field:Schema(description = "Product category", required = true)
    @field:NotNull
    val category: ProductCategoryV1Dto,

    @field:Schema(description = "Product price (NOK)", required = true, format = "float")
    @field:NotNull
    val price: Float,

    @Deprecated("To be removed in next version")
    @field:Schema(description = "Product comment", deprecated = true)
    val comment: String? = null
)
