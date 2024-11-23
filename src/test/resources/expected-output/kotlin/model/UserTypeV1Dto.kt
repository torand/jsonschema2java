package io.github.torand.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "UserTypeV1", description = "User type")
enum class UserTypeV1Dto {
    Private, Business
}
