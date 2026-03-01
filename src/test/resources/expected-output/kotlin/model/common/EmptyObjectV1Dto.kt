package io.github.torand.test.model.common

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "EmptyObjectV1", description = "TBD")
@JvmRecord
data class EmptyObjectV1Dto (

    val placeholder: String = ""
)
