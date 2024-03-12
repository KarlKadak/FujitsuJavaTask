package kkadak.fujitsutask.repository;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.model.BaseFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for interacting with the BaseFeeRule table
 */
@Repository
public interface BaseFeeRuleRepository extends JpaRepository<BaseFeeRule, Long> {

    /**
     * Returns the base fee rule for the specified city and vehicle type
     *
     * @param city        {@link kkadak.fujitsutask.enums.City} of the delivery
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @return base fee rule for the specified city and vehicle type
     */
    BaseFeeRule getBaseFeeRuleByCityAndVehicleType(City city, VehicleType vehicleType);

}
