package org.github.torand.test.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "OrderStatusV1", description = "Order status")
public enum OrderStatusV1Dto {
    Created, Processing, Dispatched
}
