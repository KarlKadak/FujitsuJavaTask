package kkadak.fujitsutask.initializers;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.ExtraFeeRuleMetric;
import kkadak.fujitsutask.enums.ExtraFeeRuleValueType;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.exceptions.IncompatibleFeeRuleException;
import kkadak.fujitsutask.model.ExtraFeeRule;
import kkadak.fujitsutask.repository.BaseFeeRuleRepository;
import kkadak.fujitsutask.repository.ExtraFeeRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

class FeeRuleInitializerTest {
    @Mock
    private BaseFeeRuleRepository baseFeeRuleRepository;
    @Mock
    private ExtraFeeRuleRepository extraFeeRuleRepository;
    @InjectMocks
    private FeeRuleInitializer feeRuleInitializer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitializeNewRule_ThrowsErrorForInvalidParameters() {
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule((City) null, VehicleType.CAR, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(City.TALLINN, null, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(City.TALLINN, VehicleType.CAR, 0D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(
                        ExtraFeeRuleMetric.PHENOMENON, ExtraFeeRuleValueType.FROM, 0D, VehicleType.CAR, 5D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(
                        ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.PHENOMENON, 0D, VehicleType.CAR, 5D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(
                        ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.FROM, 0D, null, 5D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(
                        ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.FROM, 0D, VehicleType.CAR, 0D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule((String) null, VehicleType.CAR, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule("", VehicleType.CAR, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule("testPhenomenon", null, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule("testPhenomenon", VehicleType.CAR, 0D));
    }

    @Test
    void testInitializeNewRule_ThrowsExceptionForConflictingRule() {
        when(extraFeeRuleRepository.getRules(VehicleType.CAR)).thenReturn(new ArrayList<>(){{
            add(new ExtraFeeRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL, 0D, VehicleType.CAR, 3D));
            add(new ExtraFeeRule(ExtraFeeRuleMetric.WINDSPEED, ExtraFeeRuleValueType.FROM, 10D, VehicleType.CAR, 3D));
            add(new ExtraFeeRule("testPhenomenon", VehicleType.CAR, 3D));
        }});
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.FROM,
                        -2D, VehicleType.CAR, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(ExtraFeeRuleMetric.WINDSPEED, ExtraFeeRuleValueType.UNTIL,
                        12D, VehicleType.CAR, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.UNTIL,
                        0D, VehicleType.CAR, 3D));
        assertThrowsExactly(IncompatibleFeeRuleException.class,
                () -> feeRuleInitializer.InitializeNewRule("testPhenomenon", VehicleType.CAR, 3D));
    }

    @Test
    void testInitializeNewRule_SavingValidRule() {
        assertDoesNotThrow(() -> feeRuleInitializer.InitializeNewRule(City.TALLINN, VehicleType.CAR, 3D));
        assertDoesNotThrow(
                () -> feeRuleInitializer.InitializeNewRule(ExtraFeeRuleMetric.AIRTEMP, ExtraFeeRuleValueType.FROM,
                        0D, VehicleType.CAR, 3D));
        assertDoesNotThrow(() -> feeRuleInitializer.InitializeNewRule("testPhenomenon", VehicleType.CAR, 3D));
    }

    @Test
    void initializeDefaultRules() {
        when(extraFeeRuleRepository.findAll()).thenReturn(new ArrayList<>() {{
            add(new ExtraFeeRule("testPhenomenon", VehicleType.CAR, 5D));
        }});
        assertDoesNotThrow(() -> feeRuleInitializer.InitializeDefaultRules());
    }
}
