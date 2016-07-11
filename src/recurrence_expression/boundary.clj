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

(ns recurrence-expression.boundary
  (:require [clojure.pprint :as pp]
            [clj-time.core :as t]
            [recurrence-expression.next :as n]
            [recurrence-expression.previous :as p]))

(defmulti included? (fn [time interval-pattern]
                      (cond (sequential? interval-pattern) :multiple
                            (map? interval-pattern) :single
                            (or (nil? interval-pattern) (empty? interval-pattern)) :empty
                            :else (throw (IllegalArgumentException.
                                             (str "Invalid argument: " interval-pattern))))))

(defmethod included? :multiple [time interval-pattern]
  (some #(included? time %) interval-pattern))
                      
(defmethod included? :single [time interval-pattern]
  "True if time sits inside interval described by interval pattern"
  (let [lower (p/previous time (get interval-pattern :from))
        upper (n/next-instant lower (get interval-pattern :to))
        included (or
                  (and (t/before? lower time)
                       (t/before? time upper))
                  (= lower time)
                  (= upper time))
        nested-interval (get interval-pattern :between)]
    (if included
      (if (nil? nested-interval)
        true
        (included? time nested-interval))
      false)))

(defmethod included? :empty [time interval-pattern]
  true)

(defmulti next-included-time (fn [time interval-pattern]
                      (cond (sequential? interval-pattern) :multiple
                            (map? interval-pattern) :single
                            (or (nil? interval-pattern) (empty? interval-pattern)) :empty
                            :else (throw (IllegalArgumentException.
                                             (str "Invalid argument: " interval-pattern))))))

(defmethod next-included-time :multiple [time interval-pattern]
  (let [times (map #(next-included-time time %) interval-pattern)
        sorted (sort times)]
    (if (empty? sorted)
      (throw (Exception. "Maximum time exceeded while computing next-included-time")))
    (first sorted)))

(defmethod next-included-time :single [time interval-pattern]
  "It's assumed here that time is outside interval-pattern"
  (n/next-instant time (get interval-pattern :from)))

(defmethod next-included-time :empty [time interval-pattern]
  time)
