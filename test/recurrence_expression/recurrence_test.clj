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

(ns recurrence-expression.recurrence-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.recurrence :refer :all]
            [clj-time.core :as t]))

(deftest test-compile-unit-pattern
  (testing "all or default"
    (is (= (into (sorted-set) (range 0 60)) (compile-unit-pattern nil :second true)))
    (is (= (sorted-set 0) (compile-unit-pattern nil :second false)))
    
    (is (= (into (sorted-set) (range 0 60)) (compile-unit-pattern nil :minute true)))
    (is (= (sorted-set 0) (compile-unit-pattern nil :minute false)))
    
    (is (= (into (sorted-set) (range 0 24)) (compile-unit-pattern nil :hour true)))
    (is (= (sorted-set 0) (compile-unit-pattern nil :hour false)))
    
    (is (= (into (sorted-set) (range 1 32)) (compile-unit-pattern nil :day true)))
    (is (= (sorted-set 1) (compile-unit-pattern nil :day false)))
    
    (is (= (into (sorted-set) (range 1 13)) (compile-unit-pattern nil :month true)))
    (is (= (sorted-set 1) (compile-unit-pattern nil :month false)))
    )
  )
