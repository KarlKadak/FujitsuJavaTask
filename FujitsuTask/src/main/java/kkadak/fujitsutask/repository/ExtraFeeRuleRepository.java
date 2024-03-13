package kkadak.fujitsutask.repository;

import kkadak.fujitsutask.enums.VehicleType;
import kkadak.fujitsutask.model.ExtraFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for interacting with the ExtraFeeRule table
 */
@Repository
public interface ExtraFeeRuleRepository extends JpaRepository<ExtraFeeRule, Long> {

    /**
     * Returns all extra fee rules in descending order of creation
     *
     * @return all extra fee rules in descending order of creation
     */
    List<ExtraFeeRule> findByOrderByValidFromTimestampDesc();

    /**
     * Returns the currently valid extra fee rules for specified vehicle type
     *
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @return List of currently valid extra fee rules for specified vehicle type
     */
    @Query("select r from ExtraFeeRule r"
            + " where r.vehicleType = :vehicleType"
            + " and r.validUntilTimestamp is null")
    List<ExtraFeeRule> getRules(@Param("vehicleType") VehicleType vehicleType);

    /**
     * Returns the extra fee rules for specified vehicle type which were valid during specified timestamp
     *
     * @param vehicleType {@link kkadak.fujitsutask.enums.VehicleType} used for the delivery
     * @param timestamp   latest accepted timestamp in seconds past UTC epoch
     * @return List of extra fee rules for specified vehicle type which were valid during specified timestamp
     */
    @Query("select r from ExtraFeeRule r"
            + " where r.vehicleType = :vehicleType"
            + " and r.validFromTimestamp <= :timestamp"
            + " and (r.validUntilTimestamp > :timestamp"
            + " or r.validUntilTimestamp is null)")
    List<ExtraFeeRule> getRules(@Param("vehicleType") VehicleType vehicleType, @Param("timestamp") long timestamp);
}
