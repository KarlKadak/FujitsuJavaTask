package kkadak.fujitsutask.repository;

import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.model.ExtraFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for interacting with the ExtraFeeRule table
 */
@Repository
public interface ExtraFeeRuleRepository extends JpaRepository<ExtraFeeRule, Long> {

    /**
     * Returns the extra fee rules for specified vehicle type
     *
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @return extra fee rules for specified vehicle type
     */
    List<ExtraFeeRule> getExtraFeeRulesByVehicleType(VehicleType vehicleType);

}
