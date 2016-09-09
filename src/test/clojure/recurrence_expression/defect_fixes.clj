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

(ns recurrence-expression.defect-fixes
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

(deftest defect-3082
  ;; recurrence expression actually calculated the dates correctly.
  (let [expr {:every {:week 1},
              :at {:hour 16
                   :minute 50
                   :day [{:dayOfWeek 0}
                         {:dayOfWeek 5}
                         {:dayOfWeek 6}]}}
        current-t (t/date-time 2016 8 1)
        start-t (t/date-time 2016 9 2)
        end-t (t/date-time 2016 9 5)
        t-zone (t/time-zone-for-id "US/Eastern")
        expected [(t/from-time-zone (t/date-time 2016 9 2 16 50 0) t-zone)
                  (t/from-time-zone (t/date-time 2016 9 3 16 50 0) t-zone)
                  (t/from-time-zone (t/date-time 2016 9 4 16 50 0) t-zone)]]
    (run-print-assert expr current-t start-t end-t t-zone (count expected) expected)))
