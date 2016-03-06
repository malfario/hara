(ns hara.time.data.duration-test
  (:use midje.sweet)
  (:require [hara.time.data.duration :refer :all]))

^{:refer hara.time.data.duration/adjust-year-days :added "2.2"}
(fact "calculates the number of days to be adjusted based on year"
  (adjust-year-days 1 {:year 2012 :month 2 :day 28})
  => 366

  (adjust-year-days 1 {:year 2012 :month 2 :day 29})
  => 365

  (adjust-year-days 1 {:year 2012 :month 3 :day 1})
  => 365

  (adjust-year-days 1 {:year 2011 :month 3 :day 1})
  => 366)

^{:refer hara.time.data.duration/adjust-month-days :added "2.2"}
(fact "calculates the number of days to be adjusted based on month"
  (adjust-month-days 0 2 {:year 2012 :month 3 :day 1})
  => 61
  
  (adjust-month-days 0 2 {:year 2012 :month 3 :day 1 :backward true})
  => 60

  (adjust-month-days 1 2 {:year 2011 :month 1 :day 3})
  => 60

  (adjust-month-days 1 2 {:year 2012 :month 1 :day 3})
  => 59)

^{:refer hara.time.data.duration/adjust-days :added "2.2"}
(fact "calculates the number of days to be forwarded based on year and month"
  (adjust-days {:years 0 :months 2} {:year 2012 :month 1 :day 31})
  => 60
  
  (adjust-days {:years 1 :months 2} {:year 2012 :month 1 :day 31})
  => 425

  (adjust-days {:months 2} {:year 2012 :month 1 :day 31 :backward true})
  => 62

  (adjust-days {:years 1} {:year 2013 :month 1 :day 31 :backward true})
  => 366)

^{:refer hara.time.data.duration/to-fixed-length :added "2.2"}
(fact "converts a duration map to a duration in milliseconds"
  (to-fixed-length {:days 1 :hours 3})
  => 97200000

  (to-fixed-length {:weeks 2 :days 7 :hours 53})
  => 2005200000)

^{:refer hara.time.data.duration/map-to-length :added "2.2"}
(fact "converts a duration to a length"
  (map-to-length {:months 2} {:year 2012 :month 1 :day 31 :backward true})
  => 5356800000)
