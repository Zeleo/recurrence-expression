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
            [recurrence-expression.recurrence :as r])
  (:import (org.joda.time DateTime DateTimeZone)))

(defn next-time
  ([current-time schedule]
     (let [start-time i/min-date-time
           end-time i/max-date-time]
       (next-time current-time schedule start-time end-time)))
  
  ([current-time schedule start-time]
     (next-time current-time schedule start-time i/max-date-time))
  
  ([current-time schedule start-time end-time]
     (let [current-time (v/zero-out-millis current-time)
           start-time (if (nil? start-time)
                        i/min-date-time
                        (v/zero-out-millis start-time))
           end-time (if (nil? end-time)
                      i/max-date-time
                      (v/zero-out-millis end-time))
           recurrence (get schedule :at)
           boundaries (get schedule :between)
           interval (get schedule :every)]
       (loop [time (t/plus current-time (t/seconds 1))]
         (if (or (t/after? time end-time)
                 (t/after? time i/max-date-time))
           nil
           (let [t1 (if (nil? interval)
                      time
                      (v/next-interval time schedule start-time end-time))
                 next-time (r/next-occurrence t1 recurrence)]
             (if (or (t/after? next-time end-time)
                     (t/after? next-time i/max-date-time))
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
       #_(println :n n :fire-times fire-times :current current)
       (if (= 0 n)
         fire-times
         (let [next-time (next-time current schedule start-time end-time)]
           (if (nil? next-time)
             fire-times
             (recur
              (dec n)
              (conj fire-times next-time)
              next-time)))))))
