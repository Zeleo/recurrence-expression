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

(ns recurrence-expression.core
  (:require [clojure.pprint :as pp]
            [clj-time.core :as t]
            #_[clj-time.coerce :as tc]
            [recurrence-expression.boundary :as b]
            [recurrence-expression.instant :as i]
            [recurrence-expression.interval :as v]
            [recurrence-expression.recurrence :as r]))

(declare next-time)

(defn- next-time-recurrence-interval
  [current-time interval-pattern recurrence-pattern start-time end-time]
  (let [adjusted (v/adjust-start-time interval-pattern start-time)]
    #_(println :adjusted adjusted)
    #_(println)
    (loop [time current-time]
      (if (nil? time)
        nil
        (let [end-prime (v/end-prime time interval-pattern)
              next (next-time (t/minus time (t/seconds 1))
                              {:at recurrence-pattern} time end-prime)]
          #_(println :time time :end-prime end-prime :next next)
          (if next
            next
            (recur
             (next-time time {:every interval-pattern} adjusted end-time))))))))

(defn next-time
  ([current-time schedule]
     (let [start-time i/min-date-time
           end-time i/max-date-time]
       (next-time current-time schedule start-time end-time)))
  
  ([current-time schedule start-time]
     (next-time current-time schedule start-time i/max-date-time))
  
  ([current-time schedule start-time end-time]
     (let [current-time (v/zero-out-millis current-time)
           zone (.getZone current-time)
           start-time (t/to-time-zone
                       (if (nil? start-time)
                         i/min-date-time
                         (v/zero-out-millis start-time))
                       zone)
           end-time (t/to-time-zone
                     (if (nil? end-time)
                       i/max-date-time
                       (v/zero-out-millis end-time))
                     zone)
           recurrence (get schedule :at)
           boundaries (get schedule :between)
           interval (get schedule :every)]
       (loop [time (let [plus-one (t/plus current-time (t/seconds 1))]
                     (if (t/after? plus-one start-time)
                       plus-one start-time))]
         (if (or (= time end-time)
                 (t/after? time end-time)
                 (t/after? time i/max-date-time))
           nil
           (let [next-time
                 (cond
                  (and recurrence interval) (next-time-recurrence-interval time
                                                                           interval
                                                                           recurrence
                                                                           start-time
                                                                           end-time)
                  recurrence (r/next-occurrence time recurrence)
                  interval (v/next-interval time interval start-time)
                  :else (throw (Exception.
                                (str "Invalid expression :at or :every must be defined: " schedule))))]
             (if (or (nil? next-time)
                     (t/after? next-time end-time)
                     (t/after? next-time i/max-date-time)
                     (= next-time end-time))
               nil
               (if (b/included? next-time boundaries)
                 next-time
                 (recur (b/next-included-time next-time boundaries))))))))))

(defn next-n-times
  ([current-time schedule num-times]
     (let [start-time i/min-date-time]
       (next-n-times current-time schedule num-times start-time i/max-date-time)))
  ([current-time schedule num-times start-time]
     (next-n-times current-time schedule num-times start-time i/max-date-time))
  ([current-time schedule num-times start-time end-time]
     (loop [n num-times
            fire-times []
            current current-time]
       (if (= 0 n)
         fire-times
         (let [next-time (next-time current schedule start-time end-time)]
           (if (nil? next-time)
             fire-times
             (recur
              (dec n)
              (conj fire-times next-time)
              next-time)))))))
