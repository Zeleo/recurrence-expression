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

(ns recurrence-expression.previous
  (:require [clojure.pprint :as pp]
            [clj-time.core :as t]
            [recurrence-expression.instant :refer :all])
  (:import (org.joda.time DateTime)))

(defn- previous-nth-week [base-time n day-of-week]
  (let [zone (.getZone base-time)]
    (loop [nth nil
           base-t base-time]
      (if nth
        nth
        (let [year (t/year base-t)
              month (t/month base-t)
              d (nth-week year month n day-of-week)
              day (DateTime. (t/year d)
                             (t/month d)
                             (t/day d)
                             (t/hour base-t)
                             (t/minute base-t)
                             (t/second base-t)
                             zone)]
          (recur
           (if (and day (or (= day base-time) (t/before? day base-time)))
             day
             nil)
           (t/minus base-t (t/months 1))))))))

(defn- previous-day-of-week [base-time day-of-week]
  (let [base-day-of-week (t/day-of-week base-time)
        this-many (if (< base-day-of-week day-of-week)
                    (+ (- 7 day-of-week) base-day-of-week)
                    (- base-day-of-week day-of-week))]
    (t/minus base-time (t/days this-many))))

;; Tuesdays
;; { :dayOfWeek 3 }
;;
;; Sunday of the 3rd week of the month
;; { :weekOfMonth 3 }
;;
;; 3rd Monday of the month.
;; { :dayOfWeek 2 :weekOfMonth 3 }
;;
(defn- previous-x-of-week [base-time week-pattern]
  (let [day-of-week (value-or-default (get week-pattern :dayOfWeek) 1)
        week-of-month (value-or-default (get week-pattern :weekOfMonth) nil)]
    (if week-of-month
      (previous-nth-week base-time week-of-month day-of-week)
      (previous-day-of-week base-time day-of-week))))

(defmulti #^{:private true} previous-day-of-month 
  (fn [base-time day-of-month]
    (cond
     (= :last day-of-month) :last
     (and (number? day-of-month)
          (< 0 day-of-month)
          (< day-of-month 32)) :number
     :default (throw (IllegalArgumentException.
                      (str "Invalid day-of-month: " day-of-month))))))

(defmethod #^{:private true} previous-day-of-month :last [base-time day-of-month]
  (let [prev-month (t/minus base-time (t/months 1))
        zone (.getZone base-time)]
    (t/from-time-zone (t/last-day-of-the-month (t/year prev-month)
                                               (t/month prev-month))
                      zone)))

(defmethod #^{:private true} previous-day-of-month :number [base-time day-of-month]
  (if (> day-of-month 31)
    (throw (IllegalArgumentException.
            (str "Invalid day-of-month: " day-of-month))))
  (loop [the-day nil
         base-t base-time]
    (if the-day
      the-day
      (let [base-day (t/day base-t)
            month-prior (t/minus base-t (t/months 1))
            zone (.getZone base-time)]
        (recur (if (>= base-day day-of-month)
                 (let [diff (- base-day day-of-month)]
                   (t/minus base-t (t/days diff)))
                 (safe-create-date (t/year month-prior)
                                   (t/month month-prior)
                                   day-of-month
                                   (t/hour month-prior)
                                   (t/minute month-prior)
                                   (t/second month-prior)
                                   zone))
               month-prior)))))

;; Tuesdays
;; {
;;  :day { :dayOfWeek 3 }
;;  }
;; Sunday of the 3rd week of the month
;; {
;;  :day { :weekOfMonth 3 }
;;  }
;; 3rd Monday of the month.
;; {
;;  :day { :dayOfWeek 2 :weekOfMonth 3 }
;;  }
;;
;; Wendesday and Friday of 2nd week
;; {
;;  :day { :dayOfWeek [ 4, 6 ] :weekOfMonth 2 }
;;  }
(defn- previous-day [base-time day-pattern]
  (cond
   (or (number? day-pattern)
       (= :last day-pattern)) (previous-day-of-month base-time day-pattern)
   (map? day-pattern) (previous-x-of-week base-time day-pattern)
   :else (throw (IllegalArgumentException.
                    (str "Invalid day-pattern: " day-pattern)))))

(defmulti #^{:private true} previous-unit-value (fn [time-unit-key base-time instant-pattern]
                                time-unit-key))

(defmethod #^{:private true} previous-unit-value :year [time-unit-key base-time instant-pattern]
  (DateTime. (if (contains? instant-pattern time-unit-key)
               (get instant-pattern :year)
               (t/year base-time))
             (t/month base-time)
             (t/day base-time)
             (t/hour base-time)
             (t/minute base-time)
             (t/second base-time)
             (.getZone base-time)))

(defmethod #^{:private true} previous-unit-value :month [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/month base-time) (get instant-pattern :month))]
      (DateTime. (if rollback
                   (t/year (t/minus base-time (t/years 1)))
                   (t/year base-time))
                 (get instant-pattern :month)
                 (t/day base-time)
                 (t/hour base-time)
                 (t/minute base-time)
                 (t/second base-time)
                 (.getZone base-time)))
    (DateTime. (t/year base-time)
               1
               (t/day base-time)
               (t/hour base-time)
               (t/minute base-time)
               (t/second base-time)
               (.getZone base-time))))

(defmethod #^{:private true} previous-unit-value :day [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (previous-day base-time (get instant-pattern time-unit-key))
    (DateTime. (t/year base-time)
               (t/month base-time)
               1
               (t/hour base-time)
               (t/minute base-time)
               (t/second base-time)
               (.getZone base-time))))

(defmethod #^{:private true} previous-unit-value :hour [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/hour base-time) (get instant-pattern :hour))
          t (if rollback
              (t/minus base-time (t/days 1))
              base-time)]
      (DateTime. (t/year t)
                 (t/month t)
                 (t/day t)
                 (get instant-pattern :hour)
                 (t/minute t)
                 (t/second t)
                 (.getZone base-time)))
    (DateTime. (t/year base-time)
               (t/month base-time)
               (t/day base-time)
               0
               (t/minute base-time)
               (t/second base-time)
               (.getZone base-time))))

(defmethod #^{:private true} previous-unit-value :minute [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/minute base-time) (get instant-pattern :minute))
          t (if rollback
              (t/minus base-time (t/hours 1))
              base-time)]
      (DateTime. (t/year t)
                 (t/month t)
                 (t/day t)
                 (t/hour t)
                 (get instant-pattern :minute)
                 (t/second t)
                 (.getZone base-time)))
    (DateTime. (t/year base-time)
               (t/month base-time)
               (t/day base-time)
               (t/hour base-time)
               0
               (t/second base-time)
               (.getZone base-time))))

(defmethod #^{:private true} previous-unit-value :second [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/second base-time) (get instant-pattern :second))
          t (if rollback
              (t/minus base-time (t/minutes 1))
              base-time)]
      (DateTime. (t/year t)
                 (t/month t)
                 (t/day t)
                 (t/hour t)
                 (t/minute t)
                 (get instant-pattern :second)
                 (.getZone base-time)))
    (DateTime. (t/year base-time)
               (t/month base-time)
               (t/day base-time)
               (t/hour base-time)
               (t/minute base-time)
               0
               (.getZone base-time))))

(defmethod #^{:private true} previous-unit-value :default [time-unit-key base-time instant-pattern]
  (throw (IllegalArgumentException. (str "Invalid time unit: " time-unit-key))))

(defn previous [base-time instant-pattern]
  (let [highest-order-property (highest-order-property-defined instant-pattern)
        highest-order-property-index (get instant-property-index-map highest-order-property)]
    (loop [time base-time
           properties instant-property-list]
      (if (or (empty? properties) (< highest-order-property-index
                                     (get instant-property-index-map (first properties))))
        time
        (let [property (first properties)
              prev-time (previous-unit-value property time instant-pattern)]
          (recur prev-time
                 (rest properties)))))))
