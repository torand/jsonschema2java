package org.github.torand.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "ProductCategoryV1", description = "Product categories")
enum class ProductCategoryV1Dto {
    HomeAppliance, Electronics, Computers
}
