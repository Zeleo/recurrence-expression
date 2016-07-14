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

(ns recurrence-expression.interval
  (:require [clojure.pprint :as pp]
            [clj-time.core :as t]
            [clj-time.periodic :as tp]
            [recurrence-expression.instant :as i]
            [recurrence-expression.recurrence :as r])
  (:import (org.joda.time DateTime DateTimeZone)))

;; Order matters: from small to big
(def interval-property-list
  [:second
   :minute
   :hour
   :day
   :week
   :month
   :year])

(def interval-property-index-map
  (reduce
   #(assoc %1 %2 (.indexOf interval-property-list %2))
   {}
   interval-property-list))

(defn zero-out-millis [dt]
  (let [mil (t/milli dt)]
    (t/minus dt (t/millis mil))))

(defn to-period [interval-pattern]
  ;; this only works if interval-pattern only has one key-value pair (which should be the case)
  (case (first (keys interval-pattern))
   :year (t/years (get interval-pattern :year))
   :month (t/months (get interval-pattern :month))
   :week (t/weeks (get interval-pattern :week))
   :day (t/days (get interval-pattern :day))
   :hour (t/hours (get interval-pattern :hour))
   :minute (t/minutes (get interval-pattern :minute))
   :second (t/seconds (get interval-pattern :second))
   (throw (IllegalArgumentException. (str "Invalid interval-pattern: " interval-pattern)))))

(defn to-sunday [time]
  ;; somehow clj-time (joda time) has Monday as the beginning of week
  (let [diff (- 7 (t/day-of-week time))]
    (t/plus time (t/days diff))))

(defn to-monday [time]
  ;; somehow clj-time (joda time) has Monday as the beginning of week
  (let [diff (- (t/day-of-week time) 1)]
    (t/minus time (t/days diff))))

(defn max-out-lower [time unit-keyword]
  (let [zone (.getZone time)
        utc-t (case unit-keyword
                :year (t/date-time (t/year time) 12 31 23 59 59)
                :month (let [last-day (t/last-day-of-the-month time)]
                         (t/date-time (t/year last-day) (t/month last-day) (t/day last-day) 23 59 59))
                :week (to-sunday
                       (t/date-time (t/year time) (t/month time) (t/day time) 23 59 59))
                :day (t/date-time (t/year time) (t/month time) (t/day time) 23 59 59)
                :hour (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) 59 59)
                :minute (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) (t/minute time) 59)
                :second (t/plus (zero-out-millis time) (t/seconds 1))
                (throw (IllegalArgumentException. (str "Invalid unit-keyword: " unit-keyword))))]
    (t/from-time-zone utc-t zone)))

(defn zero-out-lower [time unit-keyword]
  (let [zone (.getZone time)
        utc-t (case unit-keyword
                :year (t/date-time (t/year time) 1 1 0 0 0)
                :month (t/date-time (t/year time) (t/month time) 1 0 0 0)
                :week (to-monday
                       (t/date-time (t/year time) (t/month time) (t/day time) 0 0 0))
                :day (t/date-time (t/year time) (t/month time) (t/day time) 0 0 0)
                :hour (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) 0 0)
                :minute (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) (t/minute time) 0)
                :second (zero-out-millis time)
                (throw (IllegalArgumentException. (str "Invalid unit-keyword: " unit-keyword))))]
    (t/from-time-zone utc-t zone)))

(defn- lowest-property [interval-pattern]
  (reduce #(let [index1 (get interval-property-index-map %1)
                 index2 (get interval-property-index-map %2)]
             (if (< index1 index2) %1 %2))
          (last interval-property-list) (keys interval-pattern)))

(defn adjust-start-time
  [interval-pattern start-time]
  (let [period (to-period interval-pattern)
        unit-keyword (lowest-property interval-pattern)]
    (-> start-time
        (t/minus period)
        (zero-out-lower unit-keyword))))

(defn end-prime
  [current-time interval-pattern]
  (let [unit-keyword (lowest-property interval-pattern)]
    (max-out-lower current-time unit-keyword)))

(defn next-interval
  [current-time interval-pattern start-time]
  (let [adjusted (adjust-start-time interval-pattern start-time)]
    (first (filter #(or
                     (t/after? % current-time)
                     (= % current-time))
                   (tp/periodic-seq adjusted
                                    (to-period interval-pattern))))))
