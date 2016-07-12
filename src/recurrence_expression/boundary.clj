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

(defmulti included? (fn [time boundary-pattern]
                      (cond (sequential? boundary-pattern) :multiple
                            (map? boundary-pattern) :single
                            (or (nil? boundary-pattern) (empty? boundary-pattern)) :empty
                            :else (throw (IllegalArgumentException.
                                             (str "Invalid argument: " boundary-pattern))))))

(defmethod included? :multiple [time boundary-pattern]
  (some #(included? time %) boundary-pattern))
                      
(defmethod included? :single [time boundary-pattern]
  "True if time sits inside interval described by interval pattern"
  (let [lower (p/previous time (get boundary-pattern :from))
        upper (n/next-instant lower (get boundary-pattern :to))
        included (or
                  (and (t/before? lower time)
                       (t/before? time upper))
                  (= lower time)
                  (= upper time))
        nested-interval (get boundary-pattern :between)]
    (if included
      (if (nil? nested-interval)
        true
        (included? time nested-interval))
      false)))

(defmethod included? :empty [time boundary-pattern]
  true)

(defmulti next-included-time (fn [time boundary-pattern]
                      (cond (sequential? boundary-pattern) :multiple
                            (map? boundary-pattern) :single
                            (or (nil? boundary-pattern) (empty? boundary-pattern)) :empty
                            :else (throw (IllegalArgumentException.
                                             (str "Invalid argument: " boundary-pattern))))))

(defmethod next-included-time :multiple [time boundary-pattern]
  (let [times (map #(next-included-time time %) boundary-pattern)
        sorted (sort times)]
    (if (empty? sorted)
      (throw (Exception. "Maximum time exceeded while computing next-included-time")))
    (first sorted)))

(defmethod next-included-time :single [time boundary-pattern]
  "It's assumed here that time is outside boundary-pattern"
  (n/next-instant time (get boundary-pattern :from)))

(defmethod next-included-time :empty [time boundary-pattern]
  time)
