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

(ns recurrence-expression.interval-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.interval :refer :all]
            [clj-time.core :as t]))

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

