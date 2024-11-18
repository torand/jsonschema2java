package org.github.torand.test.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "UserTypeV1", description = "Type of user")
public enum UserTypeV1Dto {
    Private, Business
}
