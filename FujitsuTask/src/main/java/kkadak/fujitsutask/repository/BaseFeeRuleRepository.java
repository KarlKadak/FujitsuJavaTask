package kkadak.fujitsutask.repository;

import kkadak.fujitsutask.enums.City;
import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.model.BaseFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for interacting with the BaseFeeRule table
 */
@Repository
public interface BaseFeeRuleRepository extends JpaRepository<BaseFeeRule, Long> {

    /**
     * Returns all base fee rules in descending order of creation
     *
     * @return all base fee rules in descending order of creation
     */
    List<BaseFeeRule> findByOrderByValidFromTimestampDesc();

    /**
     * Returns an Optional object containing the currently valid base fee rule for the specified city and vehicle type
     * valid during the specified timestamp from the table if it exists
     *
     * @param city        {@link kkadak.fujitsutask.enums.City} of the delivery
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @return Optional object containing the currently valid base fee rule for the specified city and vehicle type
     * if it exists
     */
    Optional<BaseFeeRule> findTopByCityAndVehicleTypeOrderByValidFromTimestampDesc(City city, VehicleType vehicleType);

    /**
     * Returns an Optional object containing the base fee rule for the specified city and vehicle type
     * valid during the specified timestamp from the table if it exists
     *
     * @param city        {@link kkadak.fujitsutask.enums.City} of the delivery
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @param timestamp   latest accepted timestamp in seconds past UTC epoch
     * @return Optional object containing the base fee rule for the specified city and vehicle type
     * valid during the specified timestamp from the table if it exists
     */
    Optional<BaseFeeRule>
    findTopByCityAndVehicleTypeAndValidFromTimestampLessThanEqualOrderByValidFromTimestampDesc(City city,
                                                                                               VehicleType vehicleType,
                                                                                               long timestamp);
}
