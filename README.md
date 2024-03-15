# FujitsuJavaTask

Karl Kadak's solution for Fujitsu trial task for Java programming developer internship

## Introduction

### Task

Develop a sub-functionality of the food delivery application, which calculates the delivery fee for food couriers based
on regional base fee, vehicle type, and weather conditions.

The full task description can be found in [task.md](https://github.com/KarlKadak/FujitsuJavaTask/blob/main/task.md).

Both bonus tasks specified in [task.md](https://github.com/KarlKadak/FujitsuJavaTask/blob/main/task.md) have been
completed.

### Technologies used

- Java programming language
- [Spring framework](https://spring.io/projects/spring-framework):
    - Spring Web
    - Spring Data JPA
    - H2 Database
- [Estonian Environment Agency weather API](https://www.ilmateenistus.ee/teenused/ilmainfo/eesti-vaatlusandmed-xml/)

## Configuration

The only externally configurable variable is the cron expression (frequency) used by the weather data importer. It can
be done so in the
[application.properties](https://github.com/KarlKadak/FujitsuJavaTask/blob/main/FujitsuTask/src/main/resources/application.properties)
file.

Configuration of the calculation rules can be done from the REST endpoint during runtime.

## Use

After starting the application, the API is accessible under [localhost:8080/getFee](http://localhost:8080/getFee).
The REST interface requires some input parameters in order to function.

**Note that all deliveries are forbidden without setting up a custom ruleset
or [applying the default one](#applying-the-default-ruleset).**

### REST parameters

#### Fee calculation parameters

- `city`
    - required
    - accepted values (case insensitive):
        - "Tallinn"
        - "Tartu"
        - "Pärnu" / "Parnu"
        - specifies the city the delivery takes place in
- `vehicle`
    - required
    - accepted values (case insensitive):
        - "car"
        - "scooter"
        - "bike"
    - specifies the vehicle used for the delivery
- `time`
    - optional
    - accepted value: positive integer
    - timestamp to be used for a query made for the past
    - measured in seconds past epoch ([POSIX time](https://www.epochconverter.com/))

#### Rule management parameters

- `mode`
    - required
    - accepted values:
        - "add"
        - "disable"
        - "reset"
        - "print"
        - "history"
    - used for specifying the operation mode
    - [operation modes are discussed further below](#operation-modes)
- `type`
    - required for `mode` "add"
    - accepted values:
        - "base"
        - "until"
        - "from"
        - "phenomenon"
    - specifies the type of rule to be added
    - [rule types are discussed further below](#rule-management)
- `metric`
    - required for `mode` "add"
    - accepted values:
        - "airtemp"
        - "windspeed"
        - "phenomenon"
    - specifies the metric the rule to be added applies for
- `value`
    - required for `mode` "add"
    - accepted value: double
    - specifies the value from/until/to which the rule to be added applies
- `amount`
    - required for `mode` "add"
    - accepted values:
        - double bigger than 0
        - "forbid"
    - specifies the fee amount for the rule to be added or if delivery is forbidden under given conditions
- `id`
    - required for `mode` "disable"
    - accepted value: integer (there must exist an extra fee rule with the same value)
    - used for selecting a fee rule by ID

### Rule management

The calculation system's output varies depending on the fee rules set. Fee rules are used to set regional base fees
(**RBF**) and extra fees (**ATEF**, **WSEF**, **WPEF**).

The different fees are as follows:

- **RBFs** specify the base fee for one vehicle type in one city
- **ATEFs** specify extra fees which are added to the **RBF** of a vehicle type depending on the current air
  temperature (measured in Celsius)
- **WSEFs** specify extra fees which are added to the **RBF** of a vehicle type depending on the current wind speed
  (measured in metres per second)
- **WPEFs** specify extra fees which are added to the **RBF** of a vehicle type depending on the current weather
  phenomenon or cloud coverage

Every fee rule must have a `type`.

- **RBFs** have "base" `type`
- **ATEFs** and **WSEFs** have either "until" or "from" `type`
    - "until" `type` specifies that a rule is valid until a `metric` is above a `value` (current value ≤ rule value)
    - "from" `type` specifies that a rule is valid from the point where a `metric` is the same or more than a `value`
      (current value ≥ rule value)
    - **when creating new rules, no overlapping rules with same `metric` but different `type` can exist or an error
      message is displayed with the ID of the conflicting rule**
- **WPEFs** have "phenomenon" `type`

Use of either **ATEF** or **WSEF** is specified via using a `metric` parameter:

- **ATEF** is specified with the "airtemp" `metric`
- **WSEF** is specified with the "windspeed" `metric`

Every extra fee rule must have a `value`.

- a numeric value is used for setting up **ATEFs** and **WSEFs** which specifies the value from/until (depending
  on rule `type`) which the rule applies
- a string value is used for setting up **WPEFs** which specifies the weather metric or cloud coverage during which the
  rule applies
    - **"%20" must be used in the URL instead of a space in the URL in the weather phenomenon description**

Every fee rule must have an `amount`. The `amount` can either be a positive double or a "forbid" string. In case of a
numeric value, the `amount` specifies the fee amount. A "forbid" string specifies that use of the vehicle is forbidden.

Overriding a current fee rule differs for base fee rules and extra fee rules:

- base fee rules
    - overridden by creating a new rule with same parameters but different `value`
    - previous rule can be seen in the "history" `mode`
- extra fee rules
    - overridden by using the "disable" `mode` while specifying the old rule's `id` and then creating a new rule with
      same parameters but different `value`
    - previous rule can be seen in the "history" `mode`

#### Operation modes

Operation modes specify the behaviour of the REST interface.

The different operation modes are as follows:

- "add"
    - used for adding fee rules
    - requires also setting the `city` (if `type` is "base"), `vehicle`, `type`, `metric` (if `type` is "until"/"
      from"), `value` and `amount`
      parameters
- "disable"
    - used for disabling extra fee rules
    - requires also setting the `id` parameter
- "reset"
    - used to reset any changes and apply the default ruleset
- "print"
    - used to get an overview of the currently active ruleset
- "history"
    - used to view the history of rules

### Examples of use

#### applying the default ruleset

`localhost:8080/getFee?mode=reset`

#### viewing the current ruleset

`localhost:8080/getFee?mode=print`

#### getting current fee for delivery using a car in Tallinn

`localhost:8080/getFee?vehicle=car&city=Tallinn`

#### getting fee for delivery using a bike in Tartu at March 15, 2024 12:00:00 UTC

`localhost:8080/getFee?vehicle=bike&city=Tartu&time=1710504000`

#### overriding the base fee rule for using a scooter in Pärnu to "forbidden"

`localhost:8080/getFee?mode=add&type=base&city=Pärnu&vehicle=scooter&amount=forbid`

#### creating a new extra fee rule for adding 0.5 to the fee when using a bike and air temperature ≥ 20°C

`localhost:8080/getFee?mode=add&vehicle=bike&metric=airtemp&type=from&value=20&amount=0.5`

#### creating a new extra fee rule for adding 1 to the fee when using a car and weather phenomenon = "Fog"

`localhost:8080/getFee?mode=add&vehicle=car&type=phenomenon&value=Fog&amount=1`

#### disabling extra fee rule with ID of 20

`localhost:8080/getFee?mode=disable&id=20`

#### viewing the history of rules

`localhost:8080/getFee?mode=history`

## Other documentation

### Database

The H2 database has 3 tables:

- WeatherData
    - holds weather data which has been parsed from the weather API
- BaseFeeRule
    - holds fee calculation rules for regional base fees (**RBF**)
- ExtraFeeRule
    - holds fee calculation rules for extra fees dependent on weather conditions (**ATEF**/**WSEF**/**WPEF**)

### Extensive documentation

Complete documentation of the classes, methods and fields can be explored under the
[JavaDoc pages](https://karlkadak.github.io/FujitsuJavaTask/).