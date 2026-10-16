package com.hotel.reservations.service.extras;

import com.hotel.reservations.config.ExtrasProperties;
import com.hotel.reservations.exception.InvalidServiceException;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtraServiceFactoryTest {

    private ExtraServiceFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ExtraServiceFactory(
                new ExtrasProperties(new BigDecimal("50"), new BigDecimal("15"), new BigDecimal("30")));
    }

    @Test
    void createsTheSpaAsAFlatCharge() {
        AdditionalService spa = factory.create(ServiceType.SPA, 5);

        assertThat(spa.type()).isEqualTo(ServiceType.SPA);
        assertThat(spa.name()).isEqualTo("Spa");
        assertThat(spa.cost()).isEqualByComparingTo("50.00");
    }

    @Test
    void createsTheBreakfastPricedPerNightOfTheStay() {
        assertThat(factory.create(ServiceType.BREAKFAST, 1).cost()).isEqualByComparingTo("15.00");
        assertThat(factory.create(ServiceType.BREAKFAST, 4).cost()).isEqualByComparingTo("60.00");
        assertThat(factory.create(ServiceType.BREAKFAST, 4).name()).isEqualTo("Desayuno");
    }

    @Test
    void createsTheTransferAsAFlatCharge() {
        AdditionalService transfer = factory.create(ServiceType.TRANSFER, 3);

        assertThat(transfer.name()).isEqualTo("Traslado");
        assertThat(transfer.cost()).isEqualByComparingTo("30.00");
    }

    @Test
    void everyServiceTypeHasACreator() {
        for (ServiceType type : ServiceType.values()) {
            assertThat(factory.create(type, 2).type()).isEqualTo(type);
        }
    }

    @Test
    void costsAlwaysHaveTwoDecimals() {
        assertThat(factory.create(ServiceType.SPA, 1).cost().scale()).isEqualTo(2);
        assertThat(factory.create(ServiceType.BREAKFAST, 3).cost().scale()).isEqualTo(2);
    }

    @Test
    void rejectsAMissingServiceType() {
        assertThatThrownBy(() -> factory.create(null, 2))
                .isInstanceOf(InvalidServiceException.class)
                .hasMessage("The requested service type is not valid");
    }
}
