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

(ns recurrence-expression.instant-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.instant :refer :all]
            [clj-time.core :as t]))

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

(deftest test-last-day-of-week
  (is (= (t/date-time 2015 7 27)
         (last-day-of-week 2015 7 1)))
  (is (= (t/date-time 2015 7 28)
         (last-day-of-week 2015 7 2)))
  (is (= (t/date-time 2015 7 29)
         (last-day-of-week 2015 7 3)))
  (is (= (t/date-time 2015 7 30)
         (last-day-of-week 2015 7 4)))
  (is (= (t/date-time 2015 7 31)
         (last-day-of-week 2015 7 5)))
  (is (= (t/date-time 2015 7 25)
         (last-day-of-week 2015 7 6)))
  (is (= (t/date-time 2015 7 26)
         (last-day-of-week 2015 7 7)))
  )

(deftest test-last-day-of-week-leap
  (is (= (t/date-time 2016 2 29)
         (last-day-of-week 2016 2 1)))
  (is (= (t/date-time 2016 2 23)
         (last-day-of-week 2016 2 2)))
  (is (= (t/date-time 2016 2 24)
         (last-day-of-week 2016 2 3)))
  (is (= (t/date-time 2016 2 25)
         (last-day-of-week 2016 2 4)))
  (is (= (t/date-time 2016 2 26)
         (last-day-of-week 2016 2 5)))
  (is (= (t/date-time 2016 2 27)
         (last-day-of-week 2016 2 6)))
  (is (= (t/date-time 2016 2 28)
         (last-day-of-week 2016 2 7)))
  )
