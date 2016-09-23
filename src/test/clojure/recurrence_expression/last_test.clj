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

(ns recurrence-expression.last-test
  (:require [clojure.pprint :as pp]
            [clojure.test :refer :all]
            [recurrence-expression.core :refer :all]
            [clj-time.core :as t])
  (:import (org.joda.time DateTime)))

(defn run
  [expr current-t start-face-t end-face-t t-zone count]
  (let [current-time (.withZone current-t t-zone)
        start-time (.withZoneRetainFields start-face-t t-zone)
        end-time (.withZoneRetainFields end-face-t t-zone)
        actual (next-n-times current-time expr count start-time end-time)]
    actual))

(defn run-print-assert
  [expr current-t start-face-t end-face-t t-zone count expected]
  (let [actual (run expr current-t start-face-t end-face-t t-zone count)]
    #_(println :actual)
    #_(pp/pprint actual)
    (is (= expected actual))))

(deftest last-day-single
  (let [expr {:every {:month 1}
              :at {:hour 0
                   :minute 0
                   :day :last}}
        current-t (t/date-time 2016 1 25)
        start-t (t/date-time 2016 1 1)
        end-t (t/date-time 2025 1 1)
        t-zone (t/time-zone-for-id "US/Eastern")
        expected [(t/from-time-zone (t/date-time 2016 01 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 29) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 04 30) t-zone)]]
    (run-print-assert expr current-t start-t end-t t-zone (count expected) expected)))

(deftest last-day-multiple
  (let [expr {:every {:month 1}
              :at {:hour 0
                   :minute 0
                   :day [15 :last]}}
        current-t (t/date-time 2016 1 7)
        start-t (t/date-time 2016 1 1)
        end-t (t/date-time 2025 1 1)
        t-zone (t/time-zone-for-id "US/Eastern")
        expected [(t/from-time-zone (t/date-time 2016 01 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 01 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 29) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 31) t-zone)]]
    (run-print-assert expr current-t start-t end-t t-zone (count expected) expected)))

(deftest last-week-single
  (let [expr {:every {:month 1}
              :at {:hour 0
                   :minute 0
                   :day {:dayOfWeek 4, :weekOfMonth :last}}}
        current-t (t/date-time 2016 1 7)
        start-t (t/date-time 2016 1 1)
        end-t (t/date-time 2025 1 1)
        t-zone (t/time-zone-for-id "US/Eastern")
        expected [(t/from-time-zone (t/date-time 2016 01 28) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 25) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 04 28) t-zone)]]
    (run-print-assert expr current-t start-t end-t t-zone (count expected) expected)))

(deftest last-week-multiple
  (let [expr {:every {:month 1}
              :at {:hour 0
                   :minute 0
                   :day [15 {:dayOfWeek 4, :weekOfMonth :last}]}}
        current-t (t/date-time 2016 1 7)
        start-t (t/date-time 2016 1 1)
        end-t (t/date-time 2025 1 1)
        t-zone (t/time-zone-for-id "US/Eastern")
        expected [(t/from-time-zone (t/date-time 2016 01 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 01 28) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 25) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 04 15) t-zone)
                  (t/from-time-zone (t/date-time 2016 04 28) t-zone)]]
    (run-print-assert expr current-t start-t end-t t-zone (count expected) expected)))

(deftest last-day-and-week
  (let [expr {:every {:month 1}
              :at {:hour 0
                   :minute 0
                   :day [:last {:dayOfWeek 1, :weekOfMonth :last}]}}
        current-t (t/date-time 2016 1 7)
        start-t (t/date-time 2016 1 1)
        end-t (t/date-time 2025 1 1)
        t-zone (t/time-zone-for-id "US/Eastern")
        expected [(t/from-time-zone (t/date-time 2016 01 25) t-zone)
                  (t/from-time-zone (t/date-time 2016 01 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 02 29) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 28) t-zone)
                  (t/from-time-zone (t/date-time 2016 03 31) t-zone)
                  (t/from-time-zone (t/date-time 2016 04 25) t-zone)
                  (t/from-time-zone (t/date-time 2016 04 30) t-zone)]]
    (run-print-assert expr current-t start-t end-t t-zone (count expected) expected)))
