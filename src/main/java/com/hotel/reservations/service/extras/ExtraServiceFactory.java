package com.hotel.reservations.service.extras;

import com.hotel.reservations.config.ExtrasProperties;
import com.hotel.reservations.exception.InvalidServiceException;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.ServiceType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Factory pattern: builds the right extra service (spa, breakfast, transfer)
 * from a {@link ServiceType} and the length of the stay. Callers never decide
 * how an extra is priced; adding a new extra means registering one creator here.
 */
public class ExtraServiceFactory {

    private static final int MONEY_SCALE = 2;

    /** One creator per service type; it receives the number of nights of the stay. */
    private final Map<ServiceType, IntFunction<AdditionalService>> creators = new EnumMap<>(ServiceType.class);

    public ExtraServiceFactory(ExtrasProperties extras) {
        creators.put(ServiceType.SPA,
                nights -> new AdditionalService(ServiceType.SPA, "Spa", money(extras.spa())));
        creators.put(ServiceType.BREAKFAST,
                nights -> new AdditionalService(ServiceType.BREAKFAST, "Desayuno",
                        money(extras.breakfastPerDay().multiply(BigDecimal.valueOf(nights)))));
        creators.put(ServiceType.TRANSFER,
                nights -> new AdditionalService(ServiceType.TRANSFER, "Traslado", money(extras.transfer())));
    }

    /** Builds the extra of the given type, priced for a stay of {@code nights} nights. */
    public AdditionalService create(ServiceType type, int nights) {
        IntFunction<AdditionalService> creator = type == null ? null : creators.get(type);
        if (creator == null) {
            throw new InvalidServiceException("The requested service type is not valid");
        }
        return creator.apply(nights);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
