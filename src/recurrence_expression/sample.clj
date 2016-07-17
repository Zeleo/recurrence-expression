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
            [recurrence-expression.data :refer :all]))

(def exp-1 {:every { :year 2 }
          :at { :month 1 :day 7 :hour 15 :minute 30 }})

(def exp-2 {:at { :hour 5 }})

(def exp-3 {:at { :day { :dayOfWeek 1}}})

(def exp-4 { :day { :dayOfWeek 1}})

(def exp-5 {:at { :day { :dayOfWeek 5, :weekOfMonth :last }}})

(def exp-6 {:at { :day :last}})

(def exp {:every {:week 2} :at {:hour 3}})


;; {
;;  :every { :week 13 } 
;;  }

;; 3rd Monday of the month.
;; {
;;  :day { :dayOfWeek 1 :weekOfMonth 3 }
;;  }
;; Wendesday and Friday of 2nd week
;;  { :dayOfWeek [ 3, 5 ] :weekOfMonth 2 }
;;
