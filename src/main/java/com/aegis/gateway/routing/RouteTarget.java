package com.aegis.gateway.routing;

import java.net.URI;

public sealed interface RouteTarget {

    record Direct(URI baseUri) implements RouteTarget {

        public Direct {
            if (baseUri == null) {
                throw new IllegalArgumentException("Direct URI must not be null");
            }
        }

    }

    record Service (String serviceId)  implements RouteTarget {

        public Service {

            if (serviceId == null || serviceId.isEmpty()) {
                throw new IllegalArgumentException("Service ID must not be null or empty");
            }
        }
    }
}
