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

(ns recurrence-expression.sample-outlook-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.core :refer :all]
            [clj-time.core :as t])
  (:import (org.joda.time DateTime)))

;; This file contains recurrence expressions
;; that are equivalent to patterns created using
;; Microsoft Outlook's appointment recurrence.

(deftest sample-hourly
  (let [exp {:every {:hour 5}}
        num 4]
    (let [current (t/date-time 2016 07 12 04)
          start (t/date-time 2016 07 12 02)
          end (t/date-time 2016 07 12 17 00 01)
          expected [(t/date-time 2016 07 12 07)
                    (t/date-time 2016 07 12 12)
                    (t/date-time 2016 07 12 17)]]
      (is (= expected
             (next-n-times current exp num start end))))
    
    (let [current (t/date-time 2016 07 11 23)
          start (t/date-time 2016 07 12 02)
          end (t/date-time 2016 07 12 17)
          expected [(t/date-time 2016 07 12 02)
                    (t/date-time 2016 07 12 07)
                    (t/date-time 2016 07 12 12)]]
      (is (= expected
             (next-n-times current exp num start end))))
    
    (let [zone (t/time-zone-for-id "US/Eastern")
          current (t/from-time-zone (t/date-time 2016 11 05 23) zone)
          start (t/from-time-zone (t/date-time 2016 11 05 20) zone)
          end (t/from-time-zone (t/date-time 2016 11 06 15) zone)
          expected [(t/from-time-zone (t/date-time 2016 11 06 01) zone)
                    ;; Daylight saving time ends at 2:00am, 11/06/2016
                    (t/from-time-zone (t/date-time 2016 11 06 05) zone)
                    (t/from-time-zone (t/date-time 2016 11 06 10) zone)]]
      (is (= expected
             (next-n-times current exp num start end))))
    ))

(deftest sample-daily
  (let [exp {:every {:day 5}
             :at {:hour 3 :minute 40}}
        num 4]
    (let [current (t/date-time 2016 02 25 04)
          start (t/date-time 2016 02 25 02)
          end (t/date-time 2016 03 11 03 40 01)
          expected [(t/date-time 2016 03 01 03 40)
                    (t/date-time 2016 03 06 03 40)
                    (t/date-time 2016 03 11 03 40)]]
      (is (= expected
             (next-n-times current exp num start end))))
    
    (let [current (t/date-time 2016 02 25 03)
          start (t/date-time 2016 02 25 02)
          end (t/date-time 2016 03 11 03 40)
          expected [(t/date-time 2016 02 25 03 40)
                    (t/date-time 2016 03 01 03 40)
                    (t/date-time 2016 03 06 03 40)]]
      (is (= expected
             (next-n-times current exp num start end))))
    
    (let [current (t/date-time 2016 01 01)
          start (t/date-time 2016 02 25 02)
          end (t/date-time 2016 03 06 03 40 01)
          expected [(t/date-time 2016 02 25 03 40)
                    (t/date-time 2016 03 01 03 40)
                    (t/date-time 2016 03 06 03 40)]]
      (is (= expected
             (next-n-times current exp num start end))))
    
    (let [zone (t/time-zone-for-id "US/Eastern")
          current (t/from-time-zone (t/date-time 2016 11 05 23) zone)
          start (t/from-time-zone (t/date-time 2016 11 05 20) zone)
          end (t/from-time-zone (t/date-time 2016 11 21 03 40) zone)
          expected [(t/from-time-zone (t/date-time 2016 11 06 03 40) zone)
                    ;; Daylight saving time ends at 2:00am, 11/06/2016
                    ;; But that shouldn't affect the outcome much at all.
                    (t/from-time-zone (t/date-time 2016 11 11 03 40) zone)
                    (t/from-time-zone (t/date-time 2016 11 16 03 40) zone)]]
      (is (= expected
             (next-n-times current exp num start end))))
    ))

(deftest sample-weekly
  (let [exp {:every {:week 2},
             :at {:hour 3,
                  :minute 40,
                  :day [{:dayOfWeek 1},
                        {:dayOfWeek 3},
                        {:dayOfWeek 5}]}}
        num 4]
    (let [current (t/date-time 2016 07 13)
          start (t/date-time 2016 07 12)
          end (t/date-time 2016 10 01)
          expected [(t/date-time 2016 07 13 03 40)
                    (t/date-time 2016 07 15 03 40)
                    (t/date-time 2016 07 25 03 40)
                    (t/date-time 2016 07 27 03 40)]]
      (is (= expected
             (next-n-times current exp num start end))))
    ))

        
