package org.github.torand.test.model.common

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "AddressV1", description = "TBD")
@JvmRecord
data class AddressV1Dto (

    @field:Schema(description = "Street name and number", required = true)
    @field:NotBlank
    val streetAddress: String,

    @field:Schema(description = "Zip code of postal area", required = true)
    @field:NotBlank
    val zipCode: String,

    @field:Schema(description = "City name", required = true)
    @field:NotBlank
    val city: String,

    @field:Schema(description = "ISO 3166-alpha-3 three-letter country code. Examples: NOR, SWE, DEU", required = true, pattern = "^[A-Z]{3}$")
    @field:NotBlank
    @field:Pattern(regexp = "^[A-Z]{3}$")
    val countryCode: String
)
