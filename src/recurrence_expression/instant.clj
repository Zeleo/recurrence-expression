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

(ns recurrence-expression.instant
  (:require [clj-time.core :as t]))

;; Order matters: from small to big
(def instant-property-list
  [:second
   :minute
   :hour
   :day
   :month
   :year])

(def instant-property-index-map
  (reduce
   #(assoc %1 %2 (.indexOf instant-property-list %2))
   {}
   instant-property-list))

(def instant-property-ranges
  {:second {:min 0 :max 59}
   :minute {:min 0 :max 59}
   :hour {:min 0 :max 23}
   :day {:min 1 :max 31}
   :month {:min 1 :max 12}
   :year {:min 2015 :max (+ 100 (t/year (t/now)))}})

(def max-date-time (t/minus
                    (t/date-time (+ 1 (:max (:year instant-property-ranges))))
                    (t/seconds 1)))

(def min-date-time (t/date-time 2015 1 1))

(defn highest-order-property-defined [instant-pattern]
  (let [property-keys (keys instant-pattern)]
    (cond
     (empty? property-keys) nil
     (= 1 count property-keys) (first property-keys)
     :else (reduce #(let [index1 (get instant-property-index-map %1)
                     index2 (get instant-property-index-map %2)]
                 (if (> index1 index2)
                   %1
                   %2))
                   property-keys))))

(defn value-or-default [value default]
  (if (not (nil? value))
    value
    default))

(defn safe-create-date [year month day hour minute second]
  (let [num-days (t/number-of-days-in-the-month year month)]
    (if (> day num-days)
      nil
      (t/date-time year month day hour minute second))))

(defn compare-days [date-time instant-pattern]
  (let [dt-day (t/day date-time)
        ip-day (get instant-pattern :day)]
    (compare dt-day ip-day)))

(defn last-day-of-week [year month day-of-week]
  (let [last-day (t/last-day-of-the-month year month)
        dow-of-last-day (t/day-of-week last-day)
        diff (if (>= dow-of-last-day day-of-week)
               (- dow-of-last-day day-of-week)
               (+ dow-of-last-day (- 7 day-of-week)))
        last-day-of-week (t/minus last-day (t/days diff))]
    last-day-of-week))

(defmulti nth-week
  (fn [year month n day-of-week]
    ;; 1 <= n <= 5 or :last
    (cond
     (= :last n) :last
     (and (number? n)
          (< 0 n) (< n 6)) :number
     :default (throw (IllegalArgumentException.
                      (str "Invalid n (must be :last or a number [1, 5]): " n))))))

(defmethod nth-week :number [year month n day-of-week]
  (let [first-day (t/first-day-of-the-month year month)
        dow-of-first-day (t/day-of-week first-day)
        diff (if (<= dow-of-first-day day-of-week)
               (- day-of-week dow-of-first-day)
               (+ (- 7 dow-of-first-day) day-of-week))
        first-day-of-week (t/plus first-day (t/days diff))
        nth-day-of-week (t/plus first-day-of-week (t/weeks (dec n)))]
    (if (= month (t/month nth-day-of-week))
      nth-day-of-week
      nil)))

(defmethod nth-week :last [year month n day-of-week]
  (last-day-of-week year month day-of-week))
