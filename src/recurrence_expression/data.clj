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

(ns recurrence-expression.data
  (:require clojure.core clojure.pprint)
  (:require [schema.core :as s])
  (:require [clojure.data.json :as j])
  )

(def Instant
  {
   (s/optional-key :year) s/Int
   (s/optional-key :month) s/Int
   (s/optional-key :day) s/Int
   (s/optional-key :hour) s/Int
   (s/optional-key :minute) s/Int
   (s/optional-key :second) s/Int
   })

(def Boundary
  {
   :from Instant
   :to Instant
   })

(def Interval (assoc Instant (s/optional-key :week) s/Int))

(def Recurrence
  {
   (s/optional-key :year) (s/either s/Int [s/Int])
   (s/optional-key :month) (s/either s/Int [s/Int])
   (s/optional-key :day) (s/either s/Int [s/Int])
   (s/optional-key :hour) (s/either s/Int [s/Int])
   (s/optional-key :minute) (s/either s/Int [s/Int])
   (s/optional-key :second) (s/either s/Int [s/Int])
   })

(def Schedule
  (s/either { (s/optional-key :every) Interval
              (s/optional-key :repeat) (s/either Recurrence [Recurrence]) }
            
            { (s/optional-key :between) (s/either Boundary [Boundary])
              (s/optional-key :repeat) (s/either Recurrence [Recurrence]) }))

(defn from-json [json]
  (j/read-str json :key-fn keyword))

(defn to-json [schedule]
  (j/write-str schedule))
