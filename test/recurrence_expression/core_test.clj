(ns recurrence-expression.core-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.core :refer :all]
            [recurrence-expression.data :refer :all]
            [clj-time.core :as t])
  (:import (clojure.lang ExceptionInfo)))

;;; Sample time objects

;; TODO: additionally allowed keys: :weekOfYear, :weekOfMonth,  :dayOfYear, :dayOfMonth, :dayOfWeek
(def inst1 { :year 2015, :month 3, :day 25, :hour 9, :minute 56, :second 45 })
(def inst2 { :year 2015, :month 4, :day 1, :hour 8, :minute 0, :second 44 })

(def interval1 { :from inst1, :to inst2 })

(def time1 (t/date-time 2015 3 26 10 40 0))

;;;

(deftest test-max-date-time
  ;; max-date-time should point to the last second of a year
  (is (= 12 (t/month max-date-time)))
  (is (= 31 (t/day max-date-time)))
  (is (= 23 (t/hour max-date-time)))
  (is (= 59 (t/minute max-date-time)))
  (is (= 59 (t/second max-date-time))))

(deftest test-highest-order-property-defined
  (is (= :year (highest-order-property-defined { :year 2015 :month 1 :day 1 })))
  ;; Order of input shouldn't matter.
  (is (= :year(highest-order-property-defined { :month 1 :day 1 :year 2015 })))
  (is (= :month (highest-order-property-defined { :month 1 :day 1 })))
  (is (= :hour (highest-order-property-defined { :second 1 :minute 1 :hour 0 })))
  (is (= nil (highest-order-property-defined {})))
  ;; Below is not a requirement.  Just to show that I don't verify the
  ;; keys.  
  (is (= :bogus (highest-order-property-defined {:bogus 1}))))

(deftest test-value-or-default
  (is (= :value (value-or-default :value :default)))
  (is (= :default (value-or-default nil :default)))
  ;; A subtle point below.  I wanted "false" to be a
  ;; legit value.
  (is (= false (value-or-default false :default))))

(deftest test-previous
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    
    (testing "seconds"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 27 0)
             (previous base-time { :second 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 27 10)
             (previous base-time { :second 10 }))
          "base case")
      (is (= (t/date-time 2015 3 18 13 26 46)
             (previous base-time { :second 46 }))
          "minute rolling back")
      (is (= (t/date-time 2015 3 18 13 26 59)
             (previous base-time { :second 59 }))
          "minute rolling back"))
    
    (testing "minutes"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 27 0)
             (previous base-time { :minute 27 }))
          "instant pattern implicitly asserts ':second 0'")
      (is (= (t/date-time 2015 3 18 13 0 0)
             (previous base-time { :minute 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 15 0)
             (previous base-time { :minute 15 }))
          "base case")
      (is (= (t/date-time 2015 3 18 12 28 0)
             (previous base-time { :minute 28 }))
          "hour rolling back")
      (is (= (t/date-time 2015 3 18 12 59 0)
             (previous base-time { :minute 59 }))
          "hour rolling back"))
    
    (testing "hours"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 0 0)
             (previous base-time { :hour 13 }))
          "instant pattern implicitly asserts ':minute 0 :second 0'")
      (is (= (t/date-time 2015 3 18 0 0 0)
             (previous base-time { :hour 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 7 0 0)
             (previous base-time { :hour 7 }))
          "base case")
      (is (= (t/date-time 2015 3 17 14 0 0)
             (previous base-time { :hour 14 }))
          "day rolling back")
      (is (= (t/date-time 2015 3 17 23 0 0)
             (previous base-time { :hour 23 }))
          "day rolling back"))
    
    (testing "days"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 0 0 0)
             (previous base-time { :day 18 }))
          "instant pattern implicitly asserts ':hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2015 3 1 0 0 0)
             (previous base-time { :day 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 3 7 0 0 0)
             (previous base-time { :day 7 }))
          "base case")
      (is (= (t/date-time 2015 2 19 0 0 0)
             (previous base-time { :day 19  }))
          "month rolling back"))
    
    (testing "months"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 1 0 0 0)
             (previous base-time { :month 3 }))
          "instant pattern implicitly asserts ':day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2015 1 1 0 0 0)
             (previous base-time { :month 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 2 1 0 0 0)
             (previous base-time { :month 2 }))
          "base case")
      (is (= (t/date-time 2014 7 1 0 0 0)
             (previous base-time { :month 7  }))
          "month rolling back")
      (is (= (t/date-time 2014 12 1 0 0 0)
             (previous base-time { :month 12  }))
          "month rolling back"))
    
    (testing "years"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :year 2015 :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 1 1 0 0 0)
             (previous base-time { :year 2015 }))
          "instant pattern implicitly asserts ':month 1 :day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2022 1 1 0 0 0)
             (previous (t/date-time 2030) { :year 2022 }))
          "base case"))
    ))

(deftest test-previous-end-of-month-days
  (let [base-time (t/date-time 2015 3 28 13 27 45)]
      (is (= (t/date-time 2015 1 29 0 0 0)
             (previous base-time { :day 29  }))
          "month rolling back twice because 2/29 does not exist")))

(deftest test-next-instant
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    
    (testing "seconds"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 28 0)
             (next-instant base-time { :second 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 27 50)
             (next-instant base-time { :second 50 }))
          "base case")
      (is (= (t/date-time 2015 3 18 13 28 44)
             (next-instant base-time { :second 44 }))
          "minute rolling over"))
    
    (testing "minutes"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 27 46)
             (next-instant base-time { :minute 27 :second 46 }))
          "Lower larger than base -> don't roll over")
      (is (= (t/date-time 2015 3 18 14 27 0)
             (next-instant base-time { :minute 27 }))
          "instant pattern implicitly asserts ':second 0'")
      (is (= (t/date-time 2015 3 18 14 0 0)
             (next-instant base-time { :minute 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 50 44)
             (next-instant base-time { :minute 50 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 3 18 13 50 46)
             (next-instant base-time { :minute 50 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 3 18 14 10 0)
             (next-instant base-time { :minute 10 }))
          "hour rolling over")
      (is (= (t/date-time 2015 3 18 14 10 1)
             (next-instant base-time { :minute 10 :second 1 }))
          "hour rolling over 2")
      (is (= (t/date-time 2015 3 18 14 10 59)
             (next-instant base-time { :minute 10 :second 59 }))
          "hour rolling over 3"))
    
    (testing "hours"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 3 19 13 0 0)
             (next-instant base-time { :hour 13 }))
          "instant pattern implicitly asserts ':minute 0 :second 0'")
      (is (= (t/date-time 2015 3 19 0 0 0)
             (next-instant base-time { :hour 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 14 26 44)
             (next-instant base-time { :hour 14 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 3 18 15 28 46)
             (next-instant base-time { :hour 15 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 3 19 10 0 0)
             (next-instant base-time { :hour 10 }))
          "day rolling over")
      (is (= (t/date-time 2015 3 19 12 26 1)
             (next-instant base-time { :hour 12 :minute 26 :second 1 }))
          "day rolling over 2")
      (is (= (t/date-time 2015 3 19 1 28 59)
             (next-instant base-time { :hour 1 :minute 28 :second 59 }))
          "day rolling over 3"))
    
    (testing "days"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 4 18 0 0 0)
             (next-instant base-time { :day 18 }))
          "instant pattern implicitly asserts ':hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2015 4 1 0 0 0)
             (next-instant base-time { :day 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 3 20 12 26 44)
             (next-instant base-time { :day 20 :hour 12 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 3 20 14 28 46)
             (next-instant base-time { :day 20 :hour 14 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 3 18 14 26 46)
             (next-instant base-time { :day 18 :hour 14 :minute 26 :second 46 }))
          "highest doesn't change but lower change")
      (is (= (t/date-time 2015 4 10 0 0 0)
             (next-instant base-time { :day 10 }))
          "month rolling over")
      (is (= (t/date-time 2015 4 17 12 26 1)
             (next-instant base-time { :day 17 :hour 12 :minute 26 :second 1 }))
          "month rolling over 2")
      (is (= (t/date-time 2015 4 17 1 28 59)
             (next-instant base-time { :day 17 :hour 1 :minute 28 :second 59 }))
          "month rolling over 3"))
    
    (testing "months"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2016 3 1 0 0 0)
             (next-instant base-time { :month 3 }))
          "instant pattern implicitly asserts ':day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2016 1 1 0 0 0)
             (next-instant base-time { :month 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 4 17 12 26 44)
             (next-instant base-time { :month 4 :day 17 :hour 12 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 4 19 14 28 46)
             (next-instant base-time { :month 4 :day 19 :hour 14 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2016 3 17 14 26 46)
             (next-instant base-time { :month 3 :day 17 :hour 14 :minute 26 :second 46 }))
          "highest doesn't change but lower change")
      (is (= (t/date-time 2016 2 1 0 0 0)
             (next-instant base-time { :month 2 }))
          "month rolling over")
      (is (= (t/date-time 2016 1 17 12 26 1)
             (next-instant base-time { :month 1 :day 17 :hour 12 :minute 26 :second 1 }))
          "month rolling over 2")
      (is (= (t/date-time 2016 1 17 1 28 59)
             (next-instant base-time { :month 1 :day 17 :hour 1 :minute 28 :second 59 }))
          "day rolling over 3"))
    
    (testing "years"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :year 2015 :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 1 1 0 0 0)
             (next-instant base-time { :year 2015 }))
          "instant pattern implicitly asserts ':month 1 :day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2020 2 17 12 26 44)
             (next-instant base-time { :year 2020 :month 2 :day 17 :hour 12 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2020 4 19 14 28 46)
             (next-instant base-time { :year 2020 :month 4 :day 19 :hour 14 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 4 17 14 26 46)
             (next-instant base-time { :year 2015 :month 4 :day 17 :hour 14 :minute 26 :second 46 }))
          "highest doesn't change but lower change"))
    ))

(deftest test-included?-single
  (let [time (t/date-time 2025 6 15 12 30 30)]
    (testing "seconds"
      (is (included? time { :from { :second 10 } :to { :second 40 } }))
      (is (not (included? time { :from { :second 10 } :to { :second 29 } })))
      (is (not (included? time { :from { :second 31 } :to { :second 50 } })))
      (is (included? time { :from { :second 30 } :to { :second 31 } }))
      (is (included? time { :from { :second 29 } :to { :second 30 } }))
      )

    (testing "minutes"
      (is (included? time { :from { :minute 20 } :to { :minute 40 } }))
      (is (not (included? time { :from { :minute 10 } :to { :minute 29 } })))
      (is (not (included? time { :from { :minute 31 } :to { :minute 50 } })))
      (is (included? time { :from { :minute 30 } :to { :minute 31 } }))
      (is (not (included? time { :from { :minute 29 } :to { :minute 30 } })))
      (is (included? time { :from { :minute 29 } :to { :minute 30 :second 30 } }))
      (is (included? time
                     {
                      :from
                      { :minute 20 :second 58 }
                      :to
                      { :minute 31 :second 0 }
                      }))
      )

    (testing "hours"
      (is (included? time { :from { :hour 10 } :to { :hour 14 } }))
      (is (not (included? time { :from { :hour 8 } :to { :hour 10 } })))
      (is (not (included? time { :from { :hour 14 } :to { :hour 16 } })))
      (is (included? time { :from { :hour 12 } :to { :hour 13 } }))
      (is (not (included? time { :from { :hour 11 } :to { :hour 12 } })))
      (is (included? time { :from { :hour 11 } :to { :hour 12 :minute 30 :second 30 } }))
      (is (included? time
                     {
                      :from
                      { :hour 10 :minute 20 :second 58 }
                      :to
                      { :hour 14 :minute 29 :second 0 }
                      }))
      )
    ))

(deftest test-included?-multiple
  (let [time (t/date-time 2025 6 15 12 30 30)]
    (is (included?
         time
         [ { :from { :month 2 } :to { :month 3 } }
           { :from { :second 2 } :to { :second 5 } }
           { :from { :hour 12 } :to { :hour 12 :minute 50 } } ;; <-- this one passes.
           { :from { :month 7 } :to { :month 9 } } ]))
    )
  )

(deftest test-next-included-time
  (let [time (t/date-time 2025 6 15 12 30 30)]
    (is (= (t/date-time 2025 9 1 0 0 0)
           (next-included-time
            time
            { :from { :month 9 } :to { :month 10 } })))
    (is (= (t/date-time 2026 2 14 0 0 0)
           (next-included-time
            time
            { :from { :month 2 :day 14 } :to { :month 10 :day 1 } })))
    ))

(deftest test-compile-unit-pattern
  (testing "all or default"
    (is (= (into (sorted-set) (range 0 60)) (compile-unit-pattern nil :second true)))
    (is (= (sorted-set 0) (compile-unit-pattern nil :second false)))
    
    (is (= (into (sorted-set) (range 0 60)) (compile-unit-pattern nil :minute true)))
    (is (= (sorted-set 0) (compile-unit-pattern nil :minute false)))
    
    (is (= (into (sorted-set) (range 0 24)) (compile-unit-pattern nil :hour true)))
    (is (= (sorted-set 0) (compile-unit-pattern nil :hour false)))
    
    (is (= (into (sorted-set) (range 1 32)) (compile-unit-pattern nil :day true)))
    (is (= (sorted-set 1) (compile-unit-pattern nil :day false)))
    
    (is (= (into (sorted-set) (range 1 13)) (compile-unit-pattern nil :month true)))
    (is (= (sorted-set 1) (compile-unit-pattern nil :month false)))
    )
  )

(deftest test-next-n-times-second
  (let [schedule { :at  { :second [ 0 5 15 45 ] } }
        time (t/date-time 2025 6 15 12 30 30)
        fire-times (next-n-times time schedule 5)]
    (is (= (t/date-time 2025 6 15 12 30 45)
           (get fire-times 0)))
    (is (= (t/date-time 2025 6 15 12 31 0)
           (get fire-times 1)))
    (is (= (t/date-time 2025 6 15 12 31 5)
           (get fire-times 2)))
    (is (= (t/date-time 2025 6 15 12 31 15)
           (get fire-times 3)))
    (is (= (t/date-time 2025 6 15 12 31 45)
           (get fire-times 4)))
    ))

(deftest test-next-n-times-minute
  (let [schedule { :at  { :minute [ 0 5 15 45 ] } }
        time (t/date-time 2025 6 15 12 30 30)
        fire-times (next-n-times time schedule 5)]
    (is (= (t/date-time 2025 6 15 12 45 0)
           (get fire-times 0)))
    (is (= (t/date-time 2025 6 15 13 0 0)
           (get fire-times 1)))
    (is (= (t/date-time 2025 6 15 13 5 0)
           (get fire-times 2)))
    (is (= (t/date-time 2025 6 15 13 15 0)
           (get fire-times 3)))
    (is (= (t/date-time 2025 6 15 13 45 0)
           (get fire-times 4)))
    ))

(deftest test-next-n-times-hour
  (let [schedule { :at  { :hour [ 9 17 21 ] } }
        time (t/date-time 2025 6 15 12 30 30)
        fire-times (next-n-times time schedule 6)]
    (is (= 6 (count fire-times)))
    (is (= (t/date-time 2025 6 15 17 0 0)
           (get fire-times 0)))
    (is (= (t/date-time 2025 6 15 21 0 0)
           (get fire-times 1)))
    (is (= (t/date-time 2025 6 16 9 0 0)
           (get fire-times 2)))
    (is (= (t/date-time 2025 6 16 17 0 0)
           (get fire-times 3)))
    (is (= (t/date-time 2025 6 16 21 0 0)
           (get fire-times 4)))
    (is (= (t/date-time 2025 6 17 9 0 0)
           (get fire-times 5)))
    ))

(deftest test-next-n-times-day
  (let [schedule { :at  { :day [ 5 15 25 ] } }
        time (t/date-time 2025 6 15 12 30 30)
        fire-times (next-n-times time schedule 6)]
    (is (= 6 (count fire-times)))
    (is (= (t/date-time 2025 6 25 0 0 0)
           (get fire-times 0)))
    (is (= (t/date-time 2025 7 5 0 0 0)
           (get fire-times 1)))
    (is (= (t/date-time 2025 7 15 0 0 0)
           (get fire-times 2)))
    (is (= (t/date-time 2025 7 25 0 0 0)
           (get fire-times 3)))
    (is (= (t/date-time 2025 8 5 0 0 0)
           (get fire-times 4)))
    (is (= (t/date-time 2025 8 15 0 0 0)
           (get fire-times 5)))
    ))

(deftest test-next-n-times-month
  (let [schedule { :at  { :month [ 3 6 9 ] } }
        time (t/date-time 2025 6 15 12 30 30)
        fire-times (next-n-times time schedule 6)]
    (is (= 6 (count fire-times)))
    (is (= (t/date-time 2025 9 1 0 0 0)
           (get fire-times 0)))
    (is (= (t/date-time 2026 3 1 0 0 0)
           (get fire-times 1)))
    (is (= (t/date-time 2026 6 1 0 0 0)
           (get fire-times 2)))
    (is (= (t/date-time 2026 9 1 0 0 0)
           (get fire-times 3)))
    (is (= (t/date-time 2027 3 1 0 0 0)
           (get fire-times 4)))
    (is (= (t/date-time 2027 6 1 0 0 0)
           (get fire-times 5)))
    ))

(deftest test-next-n-times-year
  (let [schedule { :at  { :year [ 2026, 2027, 2031, 2040 ] } }
        time (t/date-time 2025 6 15 12 30 30)
        this-many-times 4
        fire-times (next-n-times time schedule this-many-times)]
    (is (= this-many-times (count fire-times)))
    (is (= (t/date-time 2026 1 1 0 0 0)
           (get fire-times 0)))
    (is (= (t/date-time 2027 1 1 0 0 0)
           (get fire-times 1)))
    (is (= (t/date-time 2031 1 1 0 0 0)
           (get fire-times 2)))
    (is (= (t/date-time 2040 1 1 0 0 0)
           (get fire-times 3)))
    ))

(deftest test-next-n-times-simple-mixed
  (let [schedule { :at  {
                             :hour [ 9 10 11 12 ]
                             :minute 30
                             } }
        time (t/date-time 2025 6 15 12 30 30)
        this-many-times 5
        fire-times (next-n-times time schedule this-many-times)]
    (is (= this-many-times (count fire-times)))
    (is (= (t/date-time 2025 6 16 9 30 0)
           (get fire-times 0)))
    (is (= (t/date-time 2025 6 16 10 30 0)
           (get fire-times 1)))
    (is (= (t/date-time 2025 6 16 11 30 0)
           (get fire-times 2)))
    (is (= (t/date-time 2025 6 16 12 30 0)
           (get fire-times 3)))
    (is (= (t/date-time 2025 6 17 9 30 0)
           (get fire-times 4)))
    ))

(deftest test-compound-schedule
  ;; not a great schedule, but serves to test integration
  ;; between interval and recurrence.
  (let [schedule {
                  :between [{ :from { :hour 9 } :to { :hour 10 } }
                            { :from { :hour 13 } :to { :hour 14 } }]
                   :at [{ :minute 10 } { :minute 20 }]
                  }
        time (t/date-time 2025 6 15 12 30 30)
        this-many-times 7
        fire-times (next-n-times time schedule this-many-times)]
    (is (= this-many-times (count fire-times)))
    (is (= (t/date-time 2025 6 15 13 10 0)
           (get fire-times 0)))
    (is (= (t/date-time 2025 6 15 13 20 0)
           (get fire-times 1)))
    (is (= (t/date-time 2025 6 16 9 10 0)
           (get fire-times 2)))
    (is (= (t/date-time 2025 6 16 9 20 0)
           (get fire-times 3)))
    (is (= (t/date-time 2025 6 16 13 10 0)
           (get fire-times 4)))
    (is (= (t/date-time 2025 6 16 13 20 0)
           (get fire-times 5)))
    (is (= (t/date-time 2025 6 17 9 10 0)
           (get fire-times 6)))
    ))

(deftest test-previous-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (previous base-time { :day { :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
    (is (= (t/date-time 2015 3 18 0 0 0)
           (previous base-time { :day { :dayOfWeek 3 } }))
          "Same day with default hours, minutes, and seconds")
    (is (= (t/date-time 2015 3 18 2 30 59)
           (previous base-time { :day { :dayOfWeek 3 } :hour 2 :minute 30 :second 59 }))
          "Same day but earlier")
    (is (= (t/date-time 2015 3 11 13 27 46)
           (previous base-time { :day { :dayOfWeek 3 } :hour 13 :minute 27 :second 46 }))
          "Same day but later time, so we go a week back")
     (is (= (t/date-time 2015 3 17 0 0 0)
           (previous base-time { :day { :dayOfWeek 2 } }))
          "One day earlier")
     (is (= (t/date-time 2015 3 12 0 0 0)
           (previous base-time { :day { :dayOfWeek 4 } }))
         "One day of week later, so one week - 1 days earlier")

     (let [base-t (t/date-time 2015 3 4 13 27 45)]
       (is (= (t/date-time 2015 2 26 0 0 0)
           (previous base-t { :day { :dayOfWeek 4 } }))
         "One day of week later, so one week - 1 days earlier, with month rolling back"))
    ))

(deftest test-next-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (next-instant base-time { :day { :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
    (is (= (t/date-time 2015 3 25 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 3 } }))
          "Same day with default hours, minutes, and seconds, this rolls forward to next week")
    (is (= (t/date-time 2015 3 25 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 3 :hour 0 :minute 0 :second 0 } }))
          "Same day with default hours, minutes, and seconds, this rolls forward to next week
default values now explicitly stated")
    (is (= (t/date-time 2015 3 18 13 30 59)
           (next-instant base-time { :day { :dayOfWeek 3 } :hour 13 :minute 30 :second 59 }))
          "Same day but later")
    (is (= (t/date-time 2015 3 25 2 27 46)
           (next-instant base-time { :day { :dayOfWeek 3 } :hour 2 :minute 27 :second 46 }))
          "Same day but earlier time, so we go a week forward")
     (is (= (t/date-time 2015 3 24 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 2 } }))
          "One day day of week earlier (week rolls forward)")
     (is (= (t/date-time 2015 3 19 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 4 } }))
         "One day of week later")

     (let [base-t (t/date-time 2015 4 29 13 27 45)]
       (is (= (t/date-time 2015 5 5 0 0 0)
           (next-instant base-t { :day { :dayOfWeek 2 } }))
         "One day of week earlier, so one week + 1 days later, with month rolling forward"))
     ))

(deftest test-next-nth-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "base time same as instant pattern -> previous time should equal to base time")
    
    (is (= (t/date-time 2015 3 19 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 13 :minute 27 :second 45 }))
        "the next day")
    
    (is (= (t/date-time 2015 3 25 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 4 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the next week")
    
    (is (= (t/date-time 2015 4 21 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 2 } :hour 13 :minute 27 :second 45 }))
        "the previous day of week, so roll forward month")
    
    (is (= (t/date-time 2015 4 8 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 2 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the previous nth week, so roll forward month")
    
    (is (= (t/date-time 2015 4 21 0 0 0)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 2 } }))
        "the previous day of week, so roll forward month")
    
    (is (= (t/date-time 2015 4 8 0 0 0)
           (next-instant base-time
                         { :day { :weekOfMonth 2 :dayOfWeek 3 } }))
        "the previous nth week, so roll forward month")
    ))

(deftest test-previous-nth-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "base time same as instant pattern -> previous time should equal to base time")
    
    (is (= (t/date-time 2015 3 17 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 2 } :hour 13 :minute 27 :second 45 }))
        "the previous day")
    
    (is (= (t/date-time 2015 3 11 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 2 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the previous week")
    
    (is (= (t/date-time 2015 2 19 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 13 :minute 27 :second 45 }))
        "the next day of week, so roll back month")
    
    (is (= (t/date-time 2015 2 25 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 4 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the next nth week, so roll back month")
    
    (is (= (t/date-time 2015 2 19 0 0 0)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 4 } }))
        "the previous day of week, so roll forward month")
    
    (is (= (t/date-time 2015 2 25 0 0 0)
           (previous base-time
                         { :day { :weekOfMonth 4 :dayOfWeek 3 } }))
        "the previous nth week, so roll forward month")
    ))

(deftest test-every
  (let [s {
           :every { :month 2 }
           :at { :day { :weekOfMonth 2 :dayOfWeek 3 } :hour 14 :minute 30 :second 45 }
           }
        start-time (t/date-time 2015 4 1)
        current-time (t/date-time 2015 4 22)
        fire-times (next-n-times current-time s 3 start-time)]
    #_(clojure.pprint/pprint fire-times)
    (is (= (t/date-time 2015 6 10 14 30 45)
           (get fire-times 0)))
    (is (= (t/date-time 2015 8 12 14 30 45)
           (get fire-times 1)))
    (is (= (t/date-time 2015 10 14 14 30 45)
           (get fire-times 2)))
    )
  )

(deftest test-every-10-seconds
  (let [s { :every { :second 10 } }
        start-time (t/date-time 2015 4 1)
        current-time (t/date-time 2015 4 1 0 0 5)
        fire-times (next-n-times current-time s 3 start-time)]
    #_(clojure.pprint/pprint fire-times)
    (is (= (t/date-time 2015 4 1 0 0 10)
           (get fire-times 0)))
    (is (= (t/date-time 2015 4 1 0 0 20)
           (get fire-times 1)))
    (is (= (t/date-time 2015 4 1 0 0 30)
           (get fire-times 2)))
    )
  )

(deftest test-every-2
  (let [s {
           :every { :month 13 }
           :at { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 9 :minute 30 }
           }
        start-time (t/date-time 2015 1 1)
        current-time (t/date-time 2015 4 22)
        fire-times (next-n-times current-time s 3 start-time)]
    #_(clojure.pprint/pprint fire-times)
    (is (= (t/date-time 2016 2 18 9 30)
           (get fire-times 0)))
    (is (= (t/date-time 2017 3 16 9 30)
           (get fire-times 1)))
    (is (= (t/date-time 2018 4 19 9 30)
           (get fire-times 2)))
    )
  )

(deftest test-to-period
  (is (= (t/years 1) (to-period { :year 1 })))
  (is (= (t/months 1) (to-period { :month 1 })))
  (is (= (t/weeks 1) (to-period { :week 1 })))
  (is (= (t/days 1) (to-period { :day 1 })))
  (is (= (t/hours 1) (to-period { :hour 1 })))
  (is (= (t/minutes 1) (to-period { :minute 1 })))
  (is (= (t/seconds 1) (to-period { :second 1 })))
  )

(deftest test-to-monday
  (is (= (t/date-time 2015 5 4 12 30 56)
         (to-monday (t/date-time 2015 5 7 12 30 56))))
  (is (= (t/date-time 2015 5 4 12 30 56)
         (to-monday (t/date-time 2015 5 10 12 30 56)))) ;; sunday
  (is (= (t/date-time 2015 5 4 12 30 56)
         (to-monday (t/date-time 2015 5 4 12 30 56)))) ;; monday
  )

(deftest test-zero-out-lower
  (let [time (t/date-time 2015 10 14 14 30 45)]
    (is (= (t/date-time 2015 1 1 0 0 0)
           (zero-out-lower time :year)))
    (is (= (t/date-time 2015 10 1 0 0 0)
           (zero-out-lower time :month)))
    (is (= (t/date-time 2015 10 14 0 0 0)
           (zero-out-lower time :day)))
    (is (= (t/date-time 2015 10 14 0 0 0) 
           (zero-out-lower time :week)))
    (is (= (t/date-time 2015 10 14 14 0 0)
           (zero-out-lower time :hour)))
    (is (= (t/date-time 2015 10 14 14 30 0)
           (zero-out-lower time :minute)))
    (is (= (t/date-time 2015 10 14 14 30 45)
           (zero-out-lower time :second)))
    ))

(deftest test-to-and-from-json
  (let [s {
           :every { :month 13 }
           :at { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 9 :minute 30 }
           }
        json (to-json s)]
    (is (= s (from-json json)))))

(deftest test-every-13-months
  (let [schedule { :every { :month 13 } :at { :day { :weekOfMonth 3 :dayOfWeek 5 } } }
        start-time (t/date-time 2015 4 14 10 35 39)
        current-time (t/plus start-time (t/seconds 2))
        expected-time (t/date-time 2015 4 17 0 0 0)]
    (is (= expected-time
           (next-time current-time
                           schedule
                           start-time
                           max-date-time)))))

(deftest test-every-13-months-2
  (let [schedule { :every { :month 13 } :at { :day { :weekOfMonth 3 :dayOfWeek 5 } :hour 9 } }
        start-time (t/date-time 2015 4 10 20 57 16)
        current-time (t/date-time 2015 4 10 20 57 20)
        expected-times [(t/date-time 2015 4 17 9)
                       (t/date-time 2016 5 20 9)
                       (t/date-time 2017 6 16 9)
                       (t/date-time 2018 7 20 9)
                       (t/date-time 2019 8 16 9)]
        actual-times (next-n-times current-time schedule (count expected-times) start-time)]
    (is (= expected-times actual-times)))
  )
