package org.github.torand.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "UserTypeV1", description = "Type of user")
enum class UserTypeV1Dto {
    Private, Business
}
