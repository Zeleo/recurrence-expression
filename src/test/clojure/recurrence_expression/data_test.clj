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

(ns recurrence-expression.data-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.data :refer :all]
            [clj-time.core :as t]))

(deftest test-to-and-from-json
  (let [s {
           :every { :month 13 }
           :at { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 9 :minute 30 }
           }
        json (to-json s)]
    (is (= s (from-json json)))))

(deftest test-keyword1
  (let [expected {:every {:year 1},
                  :at {:hour 0,
                       :minute 0,
                       :month 1,
                       :day :last}} ;; <- keyword
        json "{\"every\": {\"year\": 1},
               \"at\": {\"hour\": 0,
                        \"minute\": 0,
                        \"month\": 1,
                        \"day\": \"last\"}}" ;; <- "last"
        actual (from-json json)]
    #_(println :actual)
    #_(clojure.pprint/pprint actual)
    (is (= expected actual))))

(deftest test-keyword2
  (let [expected {:every {:year 1},
                  :at {:hour 0,
                       :minute 0,
                       :month 1,
                       :day :last}} ;; <- keyword
        json "{\"every\": {\"year\": 1},
               \"at\": {\"hour\": 0,
                        \"minute\": 0,
                        \"month\": 1,
                        \"day\": \":last\"}}" ;; <- ":last"
        actual (from-json json)]
    #_(println :actual)
    #_(clojure.pprint/pprint actual)
    (is (= expected actual))))
