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

(ns recurrence-expression.boundary-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.boundary :refer :all]
            [clj-time.core :as t]))

(deftest test-included?-single
  (let [time (t/date-time 2025 6 15 12 30 30)]
    (testing "seconds"
      (is (included? time { :from { :second 10 } :to { :second 40 } }))
      (is (not (included? time { :from { :second 10 } :to { :second 29 } })))
      (is (not (included? time { :from { :second 31 } :to { :second 50 } })))
      (is (included? time { :from { :second 30 } :to { :second 31 } }))
      (is (included? time { :from { :second 29 } :to { :second 30 } }))
      )

    (testing "minutes"
      (is (included? time { :from { :minute 20 } :to { :minute 40 } }))
      (is (not (included? time { :from { :minute 10 } :to { :minute 29 } })))
      (is (not (included? time { :from { :minute 31 } :to { :minute 50 } })))
      (is (included? time { :from { :minute 30 } :to { :minute 31 } }))
      (is (not (included? time { :from { :minute 29 } :to { :minute 30 } })))
      (is (included? time { :from { :minute 29 } :to { :minute 30 :second 30 } }))
      (is (included? time
                     {
                      :from
                      { :minute 20 :second 58 }
                      :to
                      { :minute 31 :second 0 }
                      }))
      )

    (testing "hours"
      (is (included? time { :from { :hour 10 } :to { :hour 14 } }))
      (is (not (included? time { :from { :hour 8 } :to { :hour 10 } })))
      (is (not (included? time { :from { :hour 14 } :to { :hour 16 } })))
      (is (included? time { :from { :hour 12 } :to { :hour 13 } }))
      (is (not (included? time { :from { :hour 11 } :to { :hour 12 } })))
      (is (included? time { :from { :hour 11 } :to { :hour 12 :minute 30 :second 30 } }))
      (is (included? time
                     {
                      :from
                      { :hour 10 :minute 20 :second 58 }
                      :to
                      { :hour 14 :minute 29 :second 0 }
                      }))
      )
    ))

(deftest test-included?-multiple
  (let [time (t/date-time 2025 6 15 12 30 30)]
    (is (included?
         time
         [ { :from { :month 2 } :to { :month 3 } }
           { :from { :second 2 } :to { :second 5 } }
           { :from { :hour 12 } :to { :hour 12 :minute 50 } } ;; <-- this one passes.
           { :from { :month 7 } :to { :month 9 } } ]))
    )
  )

(deftest test-next-included-time
  (let [time (t/date-time 2025 6 15 12 30 30)]
    (is (= (t/date-time 2025 9 1 0 0 0)
           (next-included-time
            time
            { :from { :month 9 } :to { :month 10 } })))
    (is (= (t/date-time 2026 2 14 0 0 0)
           (next-included-time
            time
            { :from { :month 2 :day 14 } :to { :month 10 :day 1 } })))
    ))
