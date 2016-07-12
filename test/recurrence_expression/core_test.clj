;; Copyright (c) 2015--2016 Bjönd, Inc.
;;
;; This file is part of Recurrence Expression.
;;
;; Recurrence Expression is free software: you can redistribute it
;; and/or modify it under the terms of the GNU Lesser General Public
;; License as published by the Free Software Foundation, either
;; version 3 of the License, or (at your option) any later version.
;;
;; Recurrence Expression is distributed in the hope that it will be
;; useful, but WITHOUT ANY WARRANTY; without even the implied warranty
;; of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Recurrence Expression.  If not, see
;; <http://www.gnu.org/licenses/>.

(ns recurrence-expression.core-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.core :refer :all]
            [recurrence-expression.instant :as i]
            [recurrence-expression.next :as n]
            [recurrence-expression.previous :as p]
            [clj-time.core :as t])
  (:import (org.joda.time DateTime DateTimeZone))
  (:import (clojure.lang ExceptionInfo)))

;;; Sample time objects

;; TODO: additionally allowed keys: :weekOfYear, :weekOfMonth,  :dayOfYear, :dayOfMonth, :dayOfWeek
(def inst1 { :year 2015, :month 3, :day 25, :hour 9, :minute 56, :second 45 })
(def inst2 { :year 2015, :month 4, :day 1, :hour 8, :minute 0, :second 44 })

(def interval1 { :from inst1, :to inst2 })

(def time1 (t/date-time 2015 3 26 10 40 0))

;;;

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

(deftest test-every-13-months
  (let [schedule { :every { :month 13 } :at { :day { :weekOfMonth 3 :dayOfWeek 5 } } }
        start-time (t/date-time 2015 4 14 10 35 39)
        current-time (t/plus start-time (t/seconds 2))
        expected-time (t/date-time 2015 4 17 0 0 0)]
    (is (= expected-time
           (next-time current-time
                           schedule
                           start-time
                           i/max-date-time)))))

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

(deftest test-leap-years
  (let [exp { :at { :month 2 :day 29 } }
        start-time (t/date-time 2015)
        end-time (t/date-time 2030)
        current-time start-time
        expected-times [(t/date-time 2016 2 29)
                        (t/date-time 2020 2 29)
                        (t/date-time 2024 2 29)
                        (t/date-time 2028 2 29)]
        actual-times (next-n-times current-time exp (count expected-times)
                                   start-time end-time)]
    (is (= expected-times actual-times))))

(deftest test-not-quite-leap-years
  (let [exp { :at { :month 2 :day 28 } }
        start-time (t/date-time 2015)
        end-time (t/date-time 2030)
        current-time start-time
        expected-times [(t/date-time 2015 2 28)
                        (t/date-time 2016 2 28)
                        (t/date-time 2017 2 28)
                        (t/date-time 2018 2 28)]
        actual-times (next-n-times current-time exp (count expected-times)
                                   start-time end-time)]
    (is (= expected-times actual-times))))

(deftest test-end-time
  (let [exp { :at { :month 2 :day 28 } }
        start-time (t/date-time 2015)
        end-time (t/date-time 2017 12 31)
        current-time start-time
        expected-times [(t/date-time 2015 2 28)
                        (t/date-time 2016 2 28)
                        (t/date-time 2017 2 28)]
        actual-times (next-n-times current-time exp (+ 2 (count expected-times))
                                   start-time end-time)]
    ;; we told next-n-times to return two more than expected-times,
    ;; but it correctly returns the expected-times.  It's because of
    ;; end-time.
    (is (= expected-times actual-times))))

(deftest test-last-keyword-day-of-month
  (let [expr { :day :last }
        t (t/date-time 2015 7 7 10 36)
        expected-next (t/date-time 2015 7 31)
        expected-previous (t/date-time 2015 6 30)]
    (is (= expected-next
           (n/next-instant t expr)))
    (is (= expected-previous
           (p/previous t expr)))))

(deftest test-last-keyword-day-of-month-2
  (let [expr { :at { :day :last } }
        t (t/date-time 2015 2 14)
        expected [(t/date-time 2015 3 31)
                  (t/date-time 2015 4 30)
                  (t/date-time 2015 5 31)
                  (t/date-time 2015 6 30)]]
    (is expected
        (next-n-times t expr (count expected)))))

(deftest test-last-keyword-week-of-month
  (let [expr { :day { :dayOfWeek 3, :weekOfMonth :last } }
        t (t/date-time 2015 7 7 10 36)
        expected-next (t/date-time 2015 7 29)
        expected-previous (t/date-time 2015 6 24)]
    (is (= expected-next
           (n/next-instant t expr)))
    (is (= expected-previous
           (p/previous t expr)))))

(deftest test-last-keyword-week-of-month-2
  (let [expr { :day { :dayOfWeek 3, :weekOfMonth :last } }
        t (t/date-time 2015 2 14)
        expected [(t/date-time 2015 3 25)
                  (t/date-time 2015 4 29)
                  (t/date-time 2015 5 27)
                  (t/date-time 2015 6 24)]]
    (is expected
        (next-n-times t expr (count expected)))))

(deftest test-time-zone
  (let [zone (DateTimeZone/forID "Asia/Tokyo")
        current-time (DateTime. 2015 7 28 0 0 0 zone)
        schedule { :at { :month 1 :day 1 } }
        expected (DateTime. 2016 1 1 0 0 0 zone)
        actual (next-time current-time schedule nil nil)]
    (is (= expected actual))))
