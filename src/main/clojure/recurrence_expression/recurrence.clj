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

(ns recurrence-expression.recurrence
  (:require [clojure.pprint :as pp]
            [clojure.math.numeric-tower :as nt]
            [clj-time.core :as t]
            [recurrence-expression.instant :as i]
            [recurrence-expression.next :as n]))

(defn- last-week-of-month-pattern?
  [day-pattern]
  (and (map? day-pattern)
       (= :last (get day-pattern :weekOfMonth))))

(defmulti #^{:private true} next-day-occurrence
  (fn [base-time context day-pattern]
    (cond
     (number? day-pattern) :single
     (= day-pattern :last) :single
     (map? day-pattern) :single
     (sequential? day-pattern) :multiple
     :else (throw (IllegalArgumentException.
                   (str "Invalid day-pattern: " day-pattern))))))

(defmethod #^{:private true} next-day-occurrence :single [base-time context day-pattern]
  (let [next (n/next-day-instant base-time day-pattern)
        context (cond
                 (= day-pattern :last) (assoc context :last-day-of-month day-pattern)
                 (last-week-of-month-pattern? day-pattern) (assoc context
                                                             :last-week-of-month day-pattern)
                 :else context)]
    (if (and (= (t/year base-time) (t/year next))
             (= (t/month base-time) (t/month next)))
      [next false context] ;; didn't roll-over
      [(t/date-time (t/year next)
                    (t/month next)
                    1
                    0
                    0
                    0)
       true
       context])))

(defmethod #^{:private true} next-day-occurrence :multiple [base-time context day-pattern]
  (let [next-time-results (map #(next-day-occurrence base-time context %) day-pattern)]
    (reduce #(if (t/before? (first %1) (first %2)) %1 %2) next-time-results)))

;; made public for unit tests
(defn compile-unit-pattern [unit-pattern prop all]
  (cond
   (nil? unit-pattern) (into (sorted-set)
                             (if all
                               (range (:min (prop i/instant-property-ranges))
                                      (inc (:max (prop i/instant-property-ranges))))
                               [(:min (prop i/instant-property-ranges))]))
   (number? unit-pattern) (sorted-set unit-pattern)
   (vector? unit-pattern) (into (sorted-set) unit-pattern)
   ;; TODO: implement more unit-patterns
   :else (throw (IllegalArgumentException.
                    (str "Invalid unit-pattern: " unit-pattern)))))

(defn- compile-recurrence-pattern [recurrence-pattern]
  (let [highest-order-property (i/highest-order-property-defined recurrence-pattern)
        highest-order-property-index (get i/instant-property-index-map highest-order-property)]
    (loop [props i/instant-property-list
           compiled-patterns {}]
      (if (empty? props)
        compiled-patterns
        (let [prop (first props)
              prop-index (get i/instant-property-index-map prop)
              pattern (get recurrence-pattern prop)
              all (>= prop-index highest-order-property-index)]
          (recur
           (rest props)
           (assoc
               compiled-patterns
             prop (if (= prop :day)
                    pattern ;; We don't compile :day field.
                    (compile-unit-pattern pattern prop all)))))))))

(defn- next-value [current-value compiled-pattern]
  (let [v (subseq compiled-pattern >= current-value)]
    (if (empty? v)
      :roll-over
      (first v))))

(defn- next-second [current-time context compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :second)
        current (t/second current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      [(t/plus (t/date-time (t/year current-time)
                            (t/month current-time)
                            (t/day current-time)
                            (t/hour current-time)
                            (t/minute current-time)
                            0)
               (t/minutes 1))
       true
       context]
      [(let [increment (- next-value current)]
         (t/plus current-time (t/seconds increment)))
       false
       context])))

(defn- next-minute [current-time context compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :minute)
        current (t/minute current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      [(t/plus (t/date-time (t/year current-time)
                            (t/month current-time)
                            (t/day current-time)
                            (t/hour current-time)
                            0
                            0)
               (t/hours 1))
       true
       context]
      [(t/date-time (t/year current-time)
                    (t/month current-time)
                    (t/day current-time)
                    (t/hour current-time)
                    next-value
                    (t/second current-time))
       false
       context])))

(defn- next-hour [current-time context compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :hour)
        current (t/hour current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      [(t/plus (t/date-time (t/year current-time)
                            (t/month current-time)
                            (t/day current-time)
                            0
                            0
                            0)
               (t/days 1))
       true
       context]
      [(t/date-time (t/year current-time)
                    (t/month current-time)
                    (t/day current-time)
                    next-value
                    (t/minute current-time)
                    (t/second current-time))
       false
       context])))

(defn- next-day [current-time context compiled-recurrence-pattern recurrence-pattern]
  (let [day-pattern (get recurrence-pattern :day)]
    (if day-pattern
      (next-day-occurrence current-time context day-pattern)
      (let [day-index (get i/instant-property-index-map :day)
            highest-order-property (i/highest-order-property-defined recurrence-pattern)
            highest-order-property-index (get i/instant-property-index-map highest-order-property)
            all (> day-index highest-order-property-index)]
        (if all
          [current-time false context]
          (let [current-day (t/day current-time)]
            (if (> current-day 1)
              (let [time (t/plus current-time (t/months 1))]
                [(t/date-time (t/year time)
                              (t/month time)
                              1
                              0
                              0
                              0)
                 true])
              [current-time false context])))))))

(defn- contains-week? [recurrence-pattern]
  (let [day-pattern (get recurrence-pattern :day)]
    (cond
     (not day-pattern) false
     (sequential? day-pattern) (let [maps (filter map? day-pattern)]
                                 (if (empty? maps)
                                   false
                                   (some #(or
                                           (contains? % :weekOfMonth)
                                           (contains? % :dayOfWeek)) maps)))
     (map? day-pattern) (or (contains? day-pattern :weekOfMonth)
                            (contains? day-pattern :dayOfWeek))
     :else false)))

(defn- day-of-week
  [pattern]
  (when (not (map? pattern))
    (throw (Exception. (str "Unexpected pattern (expected a map): " pattern))))
  (i/value-or-default (get pattern :dayOfWeek) 1))

(defn- next-month [current-time context compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :month)
        current (t/month current-time)
        next-value (next-value current pattern)
        year (t/year current-time)
        day (t/day current-time)]
    (cond
     (= next-value :roll-over) [(t/plus (t/date-time year
                                                     1
                                                     1
                                                     0
                                                     0
                                                     0)
                                        (t/years 1))
                                true
                                context]
     (contains? context :last-day-of-month) [(t/date-time year
                                                          next-value
                                                          (t/day (t/last-day-of-the-month
                                                                  year next-value))
                                                          (t/hour current-time)
                                                          (t/minute current-time)
                                                          (t/second current-time))
                                             false
                                             context]
     (contains? context :last-week-of-month) [(t/date-time year
                                                           next-value
                                                           (t/day
                                                            (i/last-day-of-week
                                                             year next-value
                                                             (day-of-week
                                                              (get context :last-week-of-month))))
                                                           (t/hour current-time)
                                                           (t/minute current-time)
                                                           (t/second current-time))
                                              false
                                              context]
     (< (t/day
         (t/last-day-of-the-month
          year
          next-value)) day) [(t/plus (t/date-time year
                                                  next-value
                                                  1
                                                  0
                                                  0
                                                  0)
                                     (t/months 1))
                             true
                             context]
     :else (if (and (contains-week? compiled-recurrence-pattern)
                    (not= current next-value))
             (next-day
              (t/date-time year
                           next-value
                           1
                           (t/hour current-time)
                           (t/minute current-time)
                           (t/second current-time))
              context
              compiled-recurrence-pattern
              recurrence-pattern)
             [(t/date-time year
                           next-value
                           day
                           (t/hour current-time)
                           (t/minute current-time)
                           (t/second current-time))
              false
              context]))))

(defn- next-year [current-time context compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :year)
        current (t/year current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      (throw (Exception. "Maximum time reached"))
      [(t/date-time next-value
                    (t/month current-time)
                    
                    (cond
                     (contains? context :last-day-of-month)
                     (t/day (t/last-day-of-the-month next-value (t/month current-time)))

                     (contains? context :last-week-of-month)
                     (t/day (i/last-day-of-week
                             next-value (t/month current-time)
                             (day-of-week
                              (get context :last-week-of-month))))

                     :else
                     (t/day current-time))
                    
                    (t/hour current-time)
                    (t/minute current-time)
                    (t/second current-time))
       false
       context])))

(defn- roll-forward [current-time compiled-recurrence-pattern recurrence-pattern]
  (loop [functions [next-second next-minute next-hour next-day next-month next-year]
         time current-time
         roll-over false
         context {}]
    (if (empty? functions)
      [time roll-over]
      (let [f (first functions)
            [t ro c] (f time context compiled-recurrence-pattern recurrence-pattern)]
        (if ro
          [t ro]
          (recur (rest functions)
                 t
                 ro ;; guaranteed false
                 c)
          )))))

(defmulti next-occurrence
  (fn [current-time recurrence-patterns end-time]
    (cond (or (nil? recurrence-patterns) (empty? recurrence-patterns)) :empty
          (sequential? recurrence-patterns) :multiple
          (map? recurrence-patterns) :single
          :else (throw (IllegalArgumentException.
                           (str "Invalid argument: " recurrence-patterns))))))

(defmethod next-occurrence :empty [current-time recurrence-pattern end-time]
  current-time)

(defmethod next-occurrence :multiple [current-time recurrence-patterns end-time]
  (loop [patterns recurrence-patterns
         time i/max-date-time]
    (if (empty? patterns)
      time
      (let [pattern (first patterns)
            t (next-occurrence current-time pattern end-time)]
        (recur
         (rest patterns)
         (if (t/before? t time)
           t
           time))))))

(defmethod next-occurrence :single [current-time recurrence-pattern end-time]
  (let [compiled (compile-recurrence-pattern recurrence-pattern)
        zone (.getZone current-time)]
    (loop [time (t/from-time-zone current-time t/utc)
           keep-going true]
      (cond
       (or (= time end-time)
           (t/after? time end-time)) nil
       (not keep-going) (t/from-time-zone time zone)
       :else (let [[t roll-over] (roll-forward time compiled recurrence-pattern)]
               (recur t
                      roll-over))))))
